package actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import controllers.game.stage.{ChoosingDestination, GameEnded, WaitingForPlayers}
import dao.{CardDao, GameDao}
import models.User
import models.game.{Card, ConnectToGame, GameError, GameOver, GameState, GetAttributes, LeaveGame, Message, MonsterCard, StartGame, State, UserMessage}
import services.{AttributeCalculator, GameSetup}

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
    def sendToStage(m: Message): Unit = {
      val result = state.currentStage.receive(m, user, state)
      result match {
        case Right(newState) => newState.currentStage match {
          case GameEnded =>
            gameDao.completeGame(gameId)
            updateGameState(newState)
          case _ => updateGameState(newState)
        }
        case Left(e) => sender() ! e
      }
    }

    message match {
      case ConnectToGame(desiredGameId) if desiredGameId == gameId =>
        watchers += (user.userId -> sender())
        sender() ! GameState(state.projection(user.userId))
      case LeaveGame =>
        val result = state.currentStage.receive(LeaveGame, user, state)
        result match {
          case Right(newState) =>
            if (newState.players.isEmpty) {
              updateGameState(newState)
              gameDao.completeGame(gameId)
              notifyWatchers(_ => GameOver)
              context.stop(self)
            } else {
              updateGameState(newState)
              watchers -= user.userId
              sender() ! GameOver
            }
          case Left(e) => sender() ! e
        }
      case StartGame =>
        // Quickly shift into frozen state, we don't use updateGameState
        // because we want to avoid notifying anyone
        state = state.copy(currentStage = ChoosingDestination(0))
        setUpGame(state).map(newState => {
          updateGameState(newState)
        }).recover {
          case t: Throwable => log.error(t.getMessage)
        }
      case GetAttributes(monster, slots) =>
        val player = state.players.find(_.userId == user.userId)
        val target: Option[Card] = state.dungeon.flatMap(d => monster.flatMap(index => d.ranks.drop(index - 1).headOption.flatten))
        val monsterCard: Option[MonsterCard] = target match {
          case Some(m: MonsterCard) => Some(m)
          case _ => None
        }

        if (player.nonEmpty) {
          sender() ! AttributeCalculator.getValues(player.get, slots, monsterCard.map(m => (monster.get, m)))
        } else {
          sender() ! GameError("You aren't playing")
        }
      case m if m.validate(state) => sendToStage(m)
      case _ => sender() ! GameError("Invalid Message")
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
        notifyWatchers(userId => GameState(state.projection(userId)))
      }
    })
  }

  def notifyWatchers(getMessage: Int => Message): Unit = {
    watchers.foreach({
      case (userId, ref) => ref ! getMessage(userId)
    })
  }
}

object GameActor {
  val LookupTimeout: FiniteDuration = FiniteDuration(30, SECONDS)
  def props(gameId: Int, gameDao: GameDao, cardDao: CardDao)(implicit ec: ExecutionContext): Props = {
    Props(new GameActor(gameId, gameDao, cardDao))
  }
}
