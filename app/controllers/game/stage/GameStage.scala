package controllers.game.stage

import controllers.game.Message
import models.User
import models.game.State
import play.api.mvc.Result

import scala.concurrent.Future

abstract class GameStage {
  def receive(message: Message, user: User, state: State): Future[Result]
}

