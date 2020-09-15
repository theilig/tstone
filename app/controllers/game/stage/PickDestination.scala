package controllers.game.stage
import models.User
import models.game.{GameError, Message, State}
import play.api.libs.json.{Format, Json}

case class PickDestination(currentPlayerId: Int) extends GameStage {
  def receive(message: Message, user: User, state: State): Either[State, GameError] = ???
}

object PickDestination {
  implicit val format: Format[PickDestination] = Json.format
}
