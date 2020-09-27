package actors

import actors.GameManager.GameActorRef
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import models.User
import models.game.{Authentication, ConnectToGame, GameError, Message, UserMessage}
import play.api.libs.json.{JsValue, Json}
import services.Authenticator

import scala.concurrent.duration.{FiniteDuration, SECONDS}
import scala.concurrent.{Await, ExecutionContext}

class SocketActor(out: ActorRef, authenticator: Authenticator, gameManager: ActorRef)
                 (implicit ec: ExecutionContext) extends Actor with ActorLogging {
  var authenticatedUser: Option[User] = None
  var gameRef: Option[ActorRef] = None
  def receive: Receive = {
    case m : Message =>
      out ! Json.toJson(m)
    case messageJson: JsValue =>
      val message = messageJson.as[Message]
      message match {
        case Authentication(token) =>
          val possibleUser = authenticator.authenticate(token)
          // Blocking here because we are going to reject any messages until we auth anyway
          Await.result(possibleUser, SocketActor.UserLookupTimeout)
          possibleUser.map(user => authenticatedUser = user)
        case _ if authenticatedUser.isEmpty =>
          out ! Json.toJson(GameError("Not Authenticated"))
        case c : ConnectToGame =>
          implicit val timeout: Timeout = FiniteDuration(5, SECONDS)
          (gameManager ? c).map {
            case GameActorRef(gameActor) =>
              gameRef = Some(gameActor)
              gameActor ! UserMessage(authenticatedUser.get, c)
          }
        case _ if gameRef.isEmpty =>
          out ! Json.toJson(GameError("Not Connected to Game"))
        case m => gameRef.get ! UserMessage(authenticatedUser.get, m)
      }
    case m => log.warning("Throwing away " + m)
  }
}

object SocketActor {
  val UserLookupTimeout: FiniteDuration = FiniteDuration(30, SECONDS)

  def props(out: ActorRef, authenticator: Authenticator, gameManager: ActorRef)
           (implicit executionContext: ExecutionContext): Props =
    Props(new SocketActor(out, authenticator, gameManager))

  case object Forbidden
}


