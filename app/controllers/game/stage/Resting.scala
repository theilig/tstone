package controllers.game.stage

import models.User
import models.game.{Destroy, GameError, Message, State}
import play.api.libs.json.{Format, Json}

case class Resting(currentPlayerId: Int) extends PlayerStage {
  def receive(message: Message, user: User, state: State): Either[GameError, State] =
    message match {
      case Destroy(cardNames) => destroyCards(cardNames.values.flatten.toList, state, endTurn)
      case m => Left(GameError("Unexpected message " + m.getClass.getSimpleName))
    }
}

object Resting {
  implicit val format: Format[Resting] = Json.format
}
