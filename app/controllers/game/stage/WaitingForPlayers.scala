package controllers.game.stage
import dao.GameDao
import javax.inject.Inject
import models.User
import models.game.{JoinRequest, LeaveGame, Message, State}
import play.api.mvc.Result
import play.api.mvc.Results._

import scala.concurrent.{ExecutionContext, Future}

case object WaitingForPlayers extends GameStage {
  def receive(message: Message, user: User, gameId: Int, state: State, gameDao: GameDao)
                      (implicit executionContext: ExecutionContext): Future[Result] = {
    message match {
      case JoinRequest if state.players.exists(p => p.userId == user.userId) =>
        Future.successful(BadRequest("You are already in the player list"))
      case JoinRequest if state.players.filterNot(p => p.pending).length >= 4 =>
        Future.successful(BadRequest("The Game is Full"))
      case LeaveGame =>
        val players = state.players.filterNot(p => p.userId == user.userId)
        // We don't need to update if the person leaving hadn't requested to join yet
        if (state.players.length > players.length) {
          if (players.isEmpty) {
            gameDao.completeGame(gameId).map(changedRows => {
              if (changedRows == 1) {
                Ok("Cancelled Game")
              } else {
                BadRequest("Unable to find game to cancel")
              }
            })
          } else {
            gameDao.updateGame(gameId, state.copy(players = players, ownerId = players.head.userId)).map(_ => {
              Ok("Left game")
            })
          }
        } else {
          Future.successful(Ok("Left Game"))
        }
    }
  }
}
