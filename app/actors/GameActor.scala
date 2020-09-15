package actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import controllers.game.stage.WaitingForPlayers
import dao.{CardDao, GameDao}
import models.game.{ConnectToGame, GameError, GameOver, GameState, LeaveGame, Message, State, UserMessage}

import scala.concurrent.duration.{FiniteDuration, SECONDS}
import scala.concurrent.{Await, ExecutionContext}

class GameActor(gameId: Int, gameDao: GameDao, cardDao: CardDao)
               (implicit ec: ExecutionContext) extends Actor with ActorLogging {
  // Placeholder state while we look up the actual state
  var state: State = State(0, Nil, WaitingForPlayers)
  var watchers: Map[Int, ActorRef] = Map()
  override def preStart(): Unit = {
    val lookup = gameDao.findById(gameId).map {
      case Some(gameRow) => state = State(gameRow)
      case None => context.stop(self)
    }
    // We don't want to take any messages until we have the latest snapshot
    // from the database
    Await.result(lookup, GameActor.LookupTimeout)
  }

  override def receive: Receive = {
    case UserMessage(user, ConnectToGame(desiredGameId)) if desiredGameId == gameId =>
      watchers += (user.userId -> sender())
      sender() ! GameState(state)
    case UserMessage(user, LeaveGame) =>
      val result = state.currentStage.receive(LeaveGame, user, state)
      result match {
        case Left(newState) =>
          if (newState.players.isEmpty) {
            gameDao.completeGame(gameId)
            notifyWatchers(GameOver)
          } else {
            updateGameState(newState)
            watchers -= user.userId
            sender() ! GameOver
          }
        case Right(e: GameError) => sender() ! e
      }
      case UserMessage(user, m) =>
      val result = state.currentStage.receive(m, user, state)
      result match {
        case Left(newState) => updateGameState(newState)
        case Right(e : GameError) => sender() ! e
      }
    case m => log.warning("Throwing away " + m)
  }

  private def updateGameState(newState: State) = {
    gameDao.updateGame(gameId, newState)
    state = newState
    notifyWatchers(GameState(state))
  }

  def notifyWatchers(message: Message): Unit = {
    watchers.values.foreach(ref => {
      ref ! message
    })
  }
}

object GameActor {
  val LookupTimeout: FiniteDuration = FiniteDuration(30, SECONDS)
  def props(gameId: Int, gameDao: GameDao, cardDao: CardDao)(implicit ec: ExecutionContext): Props = {
    Props(new GameActor(gameId, gameDao, cardDao))
  }
}
