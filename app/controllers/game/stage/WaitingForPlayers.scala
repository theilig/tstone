package controllers.game.stage

import models.User
import models.game.{GameError, JoinRequest, LeaveGame, Message, State}

case object WaitingForPlayers extends GameStage {
  def receive(message: Message, user: User,state: State): Either[State, GameError] = {
    message match {
      case JoinRequest if state.players.exists(p => p.userId == user.userId) =>
        Right(GameError("You are already in the player list"))
      case JoinRequest if state.players.filterNot(p => p.pending).length >= 4 =>
        Right(GameError("The Game is Full"))
      case LeaveGame =>
        val players = state.players.filterNot(p => p.userId == user.userId)
        // We don't need to update if the person leaving hadn't requested to join yet
        if (state.players.length > players.length) {
          if (players.isEmpty) {
            Left(state.copy(players = Nil, ownerId = 0))
          } else {
            Left(state.copy(players = players, ownerId = players.head.userId))
          }
        } else {
          Left(state)
        }
    }
  }
}
