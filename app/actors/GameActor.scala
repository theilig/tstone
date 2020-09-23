package actors

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import controllers.game.stage.{PickDestination, WaitingForPlayers}
import dao.{CardDao, GameDao}
import models.User
import models.game.{ConnectToGame, GameError, GameOver, GameState, LeaveGame, Message, StartGame, State, UserMessage}
import services.GameSetup

import scala.concurrent.duration.{FiniteDuration, SECONDS}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Random

class GameActor(gameId: Int, gameDao: GameDao, cardDao: CardDao)
               (implicit ec: ExecutionContext) extends Actor with ActorLogging {
  // Placeholder state while we look up the actual state
  var state: State = State(Nil, None, None, WaitingForPlayers)
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

  private def setUpGame(state: State)(implicit ec: ExecutionContext): Future[State] = {
    val random = new Random
    val newPlayers = random.shuffle(state.players.filterNot(_.pending))
    val setup = new GameSetup(cardDao)
    setup.setupGame(state.copy(players = newPlayers))
  }


  def processUserMessage(user: User, message: Message): Unit = {
    message match {
      case ConnectToGame(desiredGameId) if desiredGameId == gameId =>
        watchers += (user.userId -> sender())
        sender() ! GameState(state)
      case LeaveGame =>
        val result = state.currentStage.receive(LeaveGame, user, state)
        result match {
          case Left(newState) =>
            if (newState.players.isEmpty) {
              updateGameState(newState)
              gameDao.completeGame(gameId)
              notifyWatchers(GameOver)
              context.stop(self)
            } else {
              updateGameState(newState)
              watchers -= user.userId
              sender() ! GameOver
            }
          case Right(e: GameError) => sender() ! e
        }
      case StartGame =>
        // Quickly shift into frozen state, we don't use updateGameState
        // because we want to avoid notifying anyone
        state = state.copy(currentStage = PickDestination(0))
        setUpGame(state).map(newState => {
          updateGameState(newState)
        }).recover {
          case t: Throwable => log.error(t.getMessage)
        }
      case m =>
        val result = state.currentStage.receive(m, user, state)
        result match {
          case Left(newState) => updateGameState(newState)
          case Right(e : GameError) => sender() ! e
        }
    }
  }

  override def receive: Receive = {
    case UserMessage(user, m) =>
      val permissionError = m.checkPermissionError(user, state)
      if (permissionError.nonEmpty) {
        sender() ! permissionError.get
      } else {
        processUserMessage(user, m)
      }
    case m => log.warning("Throwing away " + m)
  }

  private def updateGameState(newState: State): Unit = {
    gameDao.updateGame(gameId, newState).map(rowsChanged => {
      if (rowsChanged == 1) {
        state = newState
        notifyWatchers(GameState(state))
      }
    })
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
