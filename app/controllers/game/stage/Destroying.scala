package controllers.game.stage

import models.User
import models.game.{Card, Destroy, GameError, Message, State}
import play.api.libs.json.{Format, Json}

case class Destroying(currentPlayerId: Int, possibleCards: List[Card], minRequired: Int = 1, maxAllowed: Int = 1)
  extends PlayerStage {
  def receive(message: Message, user: User, state: State): Either[GameError, State] = {
    message match {
      case Destroy(cardNames) => destroyCards(cardNames, state, checkSpoils)
      case m => Left(GameError("Unexpected message " + m.getClass.getSimpleName))
    }
  }
}

object Destroying {
  implicit val format: Format[Destroying] = Json.format
}


