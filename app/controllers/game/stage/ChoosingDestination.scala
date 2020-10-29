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
    val nextStage = PlayerDiscard(
      currentPlayerId = currentPlayerId,
      playerIds = Set(),
      howMany = 0
    )
    val discardStage = state.currentPlayer.get.hand.flatMap(c =>
      c.getDungeonEffects).foldLeft(nextStage)((stage, effect) => {
        effect.effect match {
          case Some("Borrow") => stage.copy(
            playerIds = stage.playerIds ++ state.players.filterNot(p =>
              p.userId == currentPlayerId || p.hand.collect {case _: HeroCard => true}.isEmpty).map(_.userId).toSet,
            borrows = stage.borrows + 1,
            howMany = stage.howMany + 1
          )
          case Some("DiscardHero|2Cards") => stage.copy(
            playerIds = stage.playerIds ++ state.players.filterNot(p =>
              p.userId == currentPlayerId || p.hand.isEmpty).map(_.userId).toSet,
            howMany = stage.howMany + 2,
            heroesDouble = stage.heroesDouble + 1
          )
          case Some("Discard")  => stage.copy(
            playerIds = stage.playerIds ++ state.players.filterNot(p =>
              p.userId == currentPlayerId || p.hand.isEmpty).map(_.userId).toSet,
            howMany = stage.howMany + 1
          )
          case _ if effect.requiredType.contains("OtherPlayer") => stage.copy(
            playerIds = stage.playerIds ++ state.players.filterNot(p =>
              p.userId == currentPlayerId || p.hand.isEmpty).map(_.userId).toSet,
            howMany = stage.howMany + 1
          )
          case _ => stage
        }
    })

    if (discardStage.playerIds.nonEmpty) {
      addCards(c => c.getDungeonEffects, currentPlayer(state).get.hand, state).copy(currentStage = discardStage)
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
