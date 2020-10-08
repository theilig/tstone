package controllers.game.stage
import models.User
import models.game.{ChooseRest, ChooseVillage, GameError, Message, State}
import play.api.libs.json.{Format, Json}

case class ChoosingDestination(currentPlayerId: Int) extends PlayerStage {
  def receive(message: Message, user: User, state: State): Either[GameError, State] = {
    message match {
      case ChooseRest => Right(state.copy(currentStage = Resting(currentPlayerId)))
      case ChooseVillage => Right(state.copy(currentStage = Purchasing(currentPlayerId)))

      case m => Left(GameError("Unexpected message " + m.getClass.getSimpleName))
    }
  }
}

object ChoosingDestination {
  implicit val format: Format[ChoosingDestination] = Json.format
}
