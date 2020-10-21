package controllers.game.stage

import models.User
import models.game.{Card, Destroy, GameError, Message, State}
import play.api.libs.json.{Format, Json}

case class Destroying(currentPlayerId: Int, possibleCards: List[Card], monsterSpoils: List[String])
  extends PlayerStage {
  def receive(message: Message, user: User, state: State): Either[GameError, State] = {
    message match {
      case Destroy(cardNames) => destroyCards(cardNames.values.flatten.toList, state, s => {
        checkSpoils(monsterSpoils, s)
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
      case Destroy(cardNames) => destroyCards(cardNames.values.flatten.toList, state, endTurn)
      case m => Left(GameError("Unexpected message " + m.getClass.getSimpleName))
    }
  }
}

object DiscardOrDestroy {
  implicit val format: Format[DiscardOrDestroy] = Json.format
}


