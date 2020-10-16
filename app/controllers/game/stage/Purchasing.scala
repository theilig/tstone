package controllers.game.stage

import controllers.GameException
import models.User
import models.game.{Card, GameError, Message, Purchase, State, TurnEffect, VillagerCard}
import play.api.libs.json.{Format, Json}
import services.CardManager

import scala.annotation.tailrec

case class Purchasing(currentPlayerId: Int) extends PlayerStage {
  def destroyCards(hand: List[Card], me: Card, destroyed: List[Card]): (Map[String, Int], List[Card]) = {
    @tailrec
    def performDestroys(
                         destroyEffects: List[(Card, TurnEffect)],
                         cardsToDestroy: List[Card],
                         currentHand: List[Card],
                         adjustments: Map[String, Int]
                       ): (Map[String, Int], List[Card]) = {
      destroyEffects match {
        case Nil if cardsToDestroy.isEmpty => (adjustments, currentHand)
        case Nil => throw new GameException("All cards could not be destroyed")
        case (card, effect) :: remaining =>
          val matchingCard = cardsToDestroy.find(c => effect.matchesRequiredCard(c, c.getName == me.getName))
          if (matchingCard.nonEmpty) {
            performDestroys(
              remaining,
              CardManager.removeOneInstanceFromCards(cardsToDestroy, card.getName),
              CardManager.removeOneInstanceFromCards(currentHand, card.getName),
              effect.adjustAttributes(adjustments, Some(card))
            )
          } else {
            performDestroys(remaining, cardsToDestroy, currentHand, adjustments)
          }
      }
    }

    def possibleDestroys: List[(Card, TurnEffect)] = {
      me match {
        case v: VillagerCard =>
          v.villageEffects.flatMap {
            case e if e.effect.contains("Destroy") =>
              destroyed.find(c => e.matchesRequiredCard(c, c.getName == v.getName)).map(c => (c, e))
            case _ => None
          }
      }
    }

    performDestroys(possibleDestroys, destroyed, hand, Map())
  }
  def getBuyingPower(hand: List[Card], destroyed: Map[Card, List[Card]]): (Int, Int, Int, List[Card]) = {
    val (adjustments, updatedHand) = destroyed.foldLeft((
      Map("Gold" -> 0, "Experience" -> 0, "Buys" -> 0), hand
    ))((update, destroy) => {
      val (currentAdjustments, currentHand) = update
      destroy match {
        case (c, cardList) =>
          val (newAdjustments, newHand) = destroyCards(currentHand, c, cardList)
          (currentAdjustments.map {
            case (key, value) => key -> (value + newAdjustments.getOrElse(key, 0))
          }, newHand)
      }
    })
    (
      updatedHand.map(_.getGoldValue).sum + adjustments("Gold"),
      adjustments("Buys") + 1,
      adjustments("Experience"),
      updatedHand
    )
  }

  def receive(message: Message, user: User, state: State): Either[GameError, State] = {
    message match {
      case Purchase(bought, destroyed) =>
        try {
          val currentHand = currentPlayer(state).get.hand
          val destroyedCards = destroyed.map {
            case (key, list) =>
              currentHand.find(c => c.getName == key).get ->
                list.map(name => currentHand.find(c => c.getName == name).get)
          }
          val (totalGold, totalBuys, newExperience, newHand) = getBuyingPower(currentHand, destroyedCards)
          val destroyedState = state.updatePlayer(currentPlayerId)(_.copy(hand = newHand))
          val (_, _, finalTurnState) = bought.foldLeft((totalGold, totalBuys, destroyedState))((buyingPower, name) => {
            val (gold, buys, newState) = buyingPower
            if (buys <= 0) {
              throw new GameException("Bought too many cards")
            } else {
              val (stateWithCard, card) =
                CardManager.takeCard(currentPlayer(newState).get, name, newState, topOnly = true)
              if (card.getCost.exists(_ <= gold)) {
                (gold - card.getCost.get, buys - 1, stateWithCard)
              } else {
                throw new GameException(s"You cannot buy $name")
              }
            }
          })
          val finalPlayer = currentPlayer(finalTurnState).get
          val totalExperience = finalPlayer.xp + newExperience
          val potentialUpgrades = finalPlayer.hand.filter(_.canUpgrade(totalExperience))
          if (potentialUpgrades.nonEmpty) {
            Right(finalTurnState.updatePlayer(currentPlayerId)(p => p.copy(xp = totalExperience)).copy(
              currentStage = Upgrading(currentPlayerId)))
          } else {
            Right(endTurn(finalTurnState.updatePlayer(currentPlayerId)(p => p.copy(xp = p.xp + newExperience))))
          }
        } catch {
          case g: GameException => Left(GameError(g.getMessage))
        }
      case m => Left(GameError("Unexpected message " + m.getClass.getSimpleName))
    }
  }
}

object Purchasing {
  implicit val format: Format[Purchasing] = Json.format
}


