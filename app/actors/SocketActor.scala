package actors

import akka.actor.{Actor, ActorRef, Props}
import models.User
import models.game.{Authentication, Message}
import play.api.libs.json.{JsObject, JsString, JsValue}
import services.Authenticator

import scala.concurrent.duration.{FiniteDuration, SECONDS}
import scala.concurrent.{Await, ExecutionContext}

class SocketActor(out: ActorRef, authenticator: Authenticator)
                 (implicit ec: ExecutionContext) extends Actor {
  var authenticatedUser: Option[User] = None
  def receive: Receive = {
    case messageJson: JsValue =>
      val message = messageJson.as[Message]
      message match {
        case Authentication(token) =>
          val possibleUser = authenticator.authenticate(token)
          // Blocking here because we are going to reject any messages until we auth anyway
          Await.result(possibleUser, SocketActor.UserLookupTimeout)
          possibleUser.map(user => authenticatedUser = user)
        case _ if authenticatedUser.isEmpty =>
          out ! JsObject(Seq(
            "error" -> JsString("Not Authenticated")
          ))
      }
  }
}

object SocketActor {
  val UserLookupTimeout: FiniteDuration = FiniteDuration(30, SECONDS)

  def props(out: ActorRef, authenticator: Authenticator)(implicit executionContext: ExecutionContext): Props =
    Props(new SocketActor(out, authenticator))

  case object Forbidden
}


