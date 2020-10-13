package controllers.game.stage

import controllers.GameException
import models.User
import models.game._
import play.api.libs.json.{Format, Json}
import services.CardManager

case class TakingSpoils(currentPlayerId: Int, spoilsTypes: List[String]) extends PlayerStage {
  def getBuyingPower(hand: List[Card]): Int = {
    hand.map(_.getGoldValue).sum
  }

  def removeTypeFromEligible(eligibleTypes: List[String], card: Card): List[String] = {
    eligibleTypes match {
      case Nil => Nil
      case x :: xs if CardManager.matchesType(card, x) => xs
      case x :: xs => x :: removeTypeFromEligible(xs, card)
    }
  }

  def receive(message: Message, user: User, state: State): Either[GameError, State] = {
    message match {
      case Purchase(bought, _) =>
        try {
          val totalGold = getBuyingPower(state.currentPlayer.get.hand)
          val result = bought.foldLeft((totalGold, spoilsTypes, state))((buyingPower, name) => {
            val (gold, eligibleTypes, newState) = buyingPower
            val (stateWithCard, card) =
              CardManager.takeCard(currentPlayer(newState).get, name, newState, topOnly = true)
            val remainingTypes = removeTypeFromEligible(eligibleTypes, card)
            if (card.getCost.exists(_ <= gold) && remainingTypes.length < eligibleTypes.length) {
              (gold - card.getCost.get, remainingTypes, stateWithCard)
            } else {
              throw new GameException(s"You cannot take $name")
            }
          })
          Right(endTurn(result._3))
        } catch {
          case g: GameException => Left(GameError(g.getMessage))
        }
      case m => Left(GameError("Unexpected message " + m.getClass.getSimpleName))
    }
  }
}

object TakingSpoils {
  implicit val format: Format[TakingSpoils] = Json.format
}





