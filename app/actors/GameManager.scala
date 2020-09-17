package actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import akka.pattern.pipe
import dao.{CardDao, GameDao}
import models.game.ConnectToGame

import scala.concurrent.{ExecutionContext, Future}

class GameManager(gameDao: GameDao, cardDao: CardDao)
                 (implicit ec: ExecutionContext) extends Actor with ActorLogging {
  var gameActors: Map[Int, ActorRef] = Map()
  override def receive: Receive = {
    case ConnectToGame(gameId) if gameActors.contains(gameId) =>
      sender ! GameManager.GameActorRef(gameActors(gameId))
    case ConnectToGame(gameId) =>
      val response: Future[Product] = gameDao.findById(gameId).map {
        case Some(gameRow) if !gameRow.completed =>
          val gameActor = context.actorOf(GameActor.props(gameId, gameDao, cardDao), s"Game$gameId")
          context.watch(gameActor)
          gameActors += (gameId -> gameActor)
          GameManager.GameActorRef(gameActor)
        case None => GameManager.GameNotFound
      }
      response.pipeTo(sender)
    case Terminated(game) => gameActors = gameActors.filterNot({
      case (_, g) if g == game => true
      case _ => false
    })
    case m => log.warning("Throwing away " + m)
  }
}

object GameManager {
  case class GameActorRef(gameActor: ActorRef)
  case object GameNotFound
  def props(gameDao: GameDao, cardDao: CardDao)(implicit ec: ExecutionContext): Props =
    Props(new GameManager(gameDao, cardDao))
}
