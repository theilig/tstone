package controllers.game.stage
import dao.GameDao
import models.User
import models.game.{Message, State}
import play.api.libs.json.{Format, Json}
import play.api.mvc.Result

import scala.concurrent.{ExecutionContext, Future}

case class PickDestination(currentPlayerId: Int) extends GameStage {
  def receive(message: Message, user: User, gameId: Int, state: State, gameDao: GameDao)
             (implicit executionContext: ExecutionContext): Future[Result] = ???
}

object PickDestination {
  implicit val format: Format[PickDestination] = Json.format
}
