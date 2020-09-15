package controllers.game.stage

import models.{Player, User}
import models.game.{GameError, JoinGame, LeaveGame, Message, State}

case object WaitingForPlayers extends GameStage {
  val MaxPlayers = 5
  def receive(message: Message, user: User, state: State): Either[State, GameError] = {
    message match {
      case JoinGame if state.players.exists(p => p.userId == user.userId) =>
        Right(GameError("You are already in the player list"))
      case JoinGame if state.players.filterNot(p => p.pending).length >= MaxPlayers =>
        Right(GameError("The Game is Full"))
      case JoinGame =>
        val newPlayer = Player(user, pending = true)
        val newPlayerList = state.players ::: newPlayer :: Nil
        Left(state.copy(players = newPlayerList))
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
