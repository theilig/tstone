package controllers.game.stage

import models.User
import models.game.{AcceptPlayer, GameError, JoinGame, LeaveGame, Message, Player, RejectPlayer, State}

case object WaitingForPlayers extends GameStage {
  val MaxPlayers = 5

  override def currentPlayer(state: State): Option[Player] = None

  def receive(message: Message, user: User, state: State): Either[GameError, State] = {
    message match {
      case JoinGame if state.players.exists(p => p.userId == user.userId) =>
        Left(GameError("You are already in the player list"))
      case JoinGame if state.players.filterNot(p => p.pending).length >= MaxPlayers =>
        Left(GameError("The Game is Full"))
      case JoinGame =>
        val newPlayer = Player(user, pending = true)
        val newPlayerList = state.players ::: newPlayer :: Nil
        Right(state.copy(players = newPlayerList))
      case LeaveGame if state.ownerId == user.userId =>
        // use tail to drop the owner, who is the head of the active list
        val activePlayers = state.players.filterNot(_.pending).tail
        val pendingPlayers = state.players.filter(_.pending)
        val newPlayerList = if (activePlayers.isEmpty && pendingPlayers.nonEmpty) {
          // All the remaining players are pending, make first one active and the owner
          pendingPlayers.head.copy(pending = false) :: pendingPlayers.tail
        } else {
          activePlayers ::: pendingPlayers
        }
        Right(state.copy(players = newPlayerList))
      case LeaveGame =>
        val players = state.players.filterNot(p => p.userId == user.userId)
        // We don't need to update if the person leaving hadn't requested to join yet
        if (state.players.length > players.length) {
          if (players.isEmpty) {
            Right(state.copy(players = Nil))
          } else {
            Right(state.copy(players = players))
          }
        } else {
          Right(state)
        }
      case AcceptPlayer(userId) if state.players.filter(_.pending).exists(p => p.userId == userId) =>
        Right(state.updatePlayer(userId)(p => p.copy(pending = false)))
      case AcceptPlayer(userId) if state.players.exists(p => p.userId == userId) =>
        Left(GameError("Player has already been accepted"))
      case AcceptPlayer(_) =>
        Left(GameError("Player not found"))
      case RejectPlayer(userId) if state.players.filter(_.pending).exists(p => p.userId == userId) =>
        val newPlayerList = state.players.filterNot(p => p.userId == userId)
        Right(state.copy(players = newPlayerList))
      case RejectPlayer(userId) if state.players.exists(p => p.userId == userId) =>
        Left(GameError("Player has already been accepted"))
      case RejectPlayer(_) =>
        Left(GameError("Player not found"))
    }
  }
}
