package controllers.game.stage

import models.User
import models.game.{Borrowed, Card, Destroy, GameError, Message, State}
import play.api.libs.json.{Format, Json}

case class Destroying(
                       currentPlayerId: Int,
                       possibleCards: List[Card],
                       monsterSpoils: List[String],
                       borrowed: List[Borrowed])
  extends PlayerStage {
  def borrowedCardsDestroyed(borrowedDestroy: List[Borrowed]): (List[Card], List[Borrowed]) = {
    def removeBorrowed(b: Borrowed, list: List[Borrowed]): List[Borrowed] = {
      list match {
        case head :: tail if head == b => tail
        case head :: tail => head :: removeBorrowed(b, tail)
      }
    }
    def getCards(destroys: List[Borrowed], leftToBorrow: List[Borrowed]): (List[Card], List[Borrowed]) = {
      destroys match {
        case x :: remaining if leftToBorrow.contains(x) =>
          val pair = getCards(remaining, removeBorrowed(x, leftToBorrow))
          (x.card :: pair._1, pair._2)
        case Nil => (Nil, leftToBorrow)
        case _ :: remaining => getCards(remaining, leftToBorrow)
      }
    }
    getCards(borrowedDestroy, borrowed)
  }

  def receive(message: Message, user: User, state: State): Either[GameError, State] = {
    message match {
      case Destroy(cardNames, borrowedDestroy) =>
        val updatedBorrowed = borrowedCardsDestroyed(borrowedDestroy)._2
        destroyCards(cardNames.values.flatten.toList, state, s => {
        checkSpoils(monsterSpoils, updatedBorrowed, s)
      })
      case m => Left(GameError("Unexpected message " + m.getClass.getSimpleName))
    }
  }
}

object Destroying {
  implicit val format: Format[Destroying] = Json.format
}

case class DiscardOrDestroy(currentPlayerId: Int, possibleCards: List[Card]) extends PlayerStage {
  def receive(message: Message, user: User, state: State): Either[GameError, State] = {
    message match {
      case Destroy(cardNames, _) => destroyCards(cardNames.values.flatten.toList, state, endTurn)
      case m => Left(GameError("Unexpected message " + m.getClass.getSimpleName))
    }
  }
}

object DiscardOrDestroy {
  implicit val format: Format[DiscardOrDestroy] = Json.format
}


