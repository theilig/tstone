package controllers.game.stage

import models.{Player, User}
import models.game.{AcceptPlayer, GameError, JoinGame, LeaveGame, Message, RejectPlayer, StartGame, State}
import services.GameSetup

import scala.util.Random

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
        Left(state.copy(players = newPlayerList))
      case LeaveGame =>
        val players = state.players.filterNot(p => p.userId == user.userId)
        // We don't need to update if the person leaving hadn't requested to join yet
        if (state.players.length > players.length) {
          if (players.isEmpty) {
            Left(state.copy(players = Nil))
          } else {
            Left(state.copy(players = players))
          }
        } else {
          Left(state)
        }
      case AcceptPlayer(userId) if state.players.filter(_.pending).exists(p => p.userId == userId) =>
        val newPlayerList = state.players.map(p => if (p.userId == userId) { p.copy(pending = false)} else p)
        Left(state.copy(players = newPlayerList))
      case AcceptPlayer(userId) if state.players.exists(p => p.userId == userId) =>
        Right(GameError("Player has already been accepted"))
      case AcceptPlayer(_) =>
        Right(GameError("Player not found"))
      case RejectPlayer(userId) if state.players.filter(_.pending).exists(p => p.userId == userId) =>
        val newPlayerList = state.players.filterNot(p => p.userId == userId)
        Left(state.copy(players = newPlayerList))
      case RejectPlayer(userId) if state.players.exists(p => p.userId == userId) =>
        Right(GameError("Player has already been accepted"))
      case RejectPlayer(_) =>
        Right(GameError("Player not found"))
      case StartGame =>
        Left(setUpGame(state))
    }
  }
  private def setUpGame(state: State): State = {
    val random = new Random
    val newPlayers = random.shuffle(state.players.filterNot(_.pending))
    val setup = new GameSetup()
    val newState = setup.startGame(state.copy(players = newPlayers))
  }
}
