package controllers.game.stage

import models.User
import models.game._

case object GameEnded extends GameStage {
  override def currentPlayer(state: State): Option[Player] = None

  def receive(message: Message, user: User, state: State): Either[GameError, State] = {
    message match {
      case _ => Left(GameError("Game is over"))
    }
  }
}
