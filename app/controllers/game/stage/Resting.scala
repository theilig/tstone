package controllers.game.stage

import models.User
import models.game.{Destroy, GameError, Message, State}
import play.api.libs.json.{Format, Json}
import services.CardManager

case class Resting(currentPlayerId: Int) extends PlayerStage {
  def receive(message: Message, user: User, state: State): Either[GameError, State] = {
    message match {
      case Destroy(cardName: String) =>
        CardManager.destroy(currentPlayer(state).get, cardName, state) match {
          case Right(s) => Right(CardManager.discardHand(currentPlayer(s).get, s))
          case left => left
        }
      case m => Left(GameError("Unexpected message " + m.getClass.getSimpleName))
    }
  }
}

object Resting {
  implicit val format: Format[Resting] = Json.format
}
