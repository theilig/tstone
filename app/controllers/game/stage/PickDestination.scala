package controllers.game.stage
import models.User
import models.game.{GameError, Message, State}
import play.api.libs.json.{Format, Json}

case class PickDestination(override val currentPlayerId: Int) extends PlayerStage(currentPlayerId) {
  def receive(message: Message, user: User, state: State): Either[State, GameError] = ???

  override def currentPlayer: Int = currentPlayerId
}

object PickDestination {
  implicit val format: Format[PickDestination] = Json.format
}
