package controllers.game.stage
import models.User
import models.game.{Card, ChooseDungeon, ChooseRest, ChooseVillage, GameError, HeroCard, Message, State, TurnEffect}
import play.api.libs.json.{Format, Json}
import services.CardManager

import scala.annotation.tailrec

case class ChoosingDestination(currentPlayerId: Int) extends PlayerStage {
  @tailrec
  private def addCards(effects: Card => List[TurnEffect], cardsToCheck: List[Card], state: State): State = {
    cardsToCheck match {
      case Nil => state
      case c :: remaining =>
        val (newCards, newState) = effects(c).foldLeft(List[Card](), state)((currentStatus, e) => {
          val (addedCards, currentState) = currentStatus
          if (e.requiredType.isEmpty && e.adjustment.exists(o => o.attribute == "Card")) {
            CardManager.givePlayerCards(currentPlayer(state).get, e.adjustment.get.amount, currentState)
          } else {
            (addedCards, currentState)
          }
        })
        addCards(effects, remaining ::: newCards, newState)
    }
  }

  def enterDungeon(state: State): State = {
    val playersToBorrowFrom = if (state.currentPlayer.get.hand.exists(c => c.getDungeonEffects.exists(e => {
      e.effect.contains("Borrow")
    }))) {
      state.players.filter(p => p.userId != currentPlayerId && p.hand.exists({
        case _: HeroCard => true
        case _ => false
      }))
    } else {
      Nil
    }
    if (playersToBorrowFrom.nonEmpty) {
      state.copy(currentStage = BorrowHeroes(currentPlayerId, playersToBorrowFrom))
    } else {
      addCards(c => c.getDungeonEffects, currentPlayer(state).get.hand, state).copy(
        currentStage = Crawling(currentPlayerId))
    }
  }

  def receive(message: Message, user: User, state: State): Either[GameError, State] =
    message match {
      case ChooseRest => Right(state.copy(currentStage = Resting(currentPlayerId)))
      case ChooseVillage => Right(addCards(c => c.getVillageEffects, currentPlayer(state).get.hand, state).copy(
        currentStage = Purchasing(currentPlayerId))
      )
      case ChooseDungeon => Right(enterDungeon(state))

      case m => Left(GameError("Unexpected message " + m.getClass.getSimpleName))
    }
}

object ChoosingDestination {
  implicit val format: Format[ChoosingDestination] = Json.format
}
