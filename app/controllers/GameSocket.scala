package controllers
import actors.{GameManager, SocketActor}
import play.api.mvc._
import play.api.libs.streams.ActorFlow
import javax.inject.{Inject, Singleton}
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.Materializer
import dao.{CardDao, GameDao}
import play.api.libs.json.JsValue
import services.Authenticator

import scala.concurrent.ExecutionContext

@Singleton
class GameSocket @Inject()(cc: ControllerComponents, authenticator: Authenticator, gameDao: GameDao, cardDao: CardDao)
                          (implicit system: ActorSystem, mat: Materializer, ec: ExecutionContext)
  extends AbstractController(cc) {
  val gameManager: ActorRef = system.actorOf(GameManager.props(gameDao, cardDao))
  def socket: WebSocket = WebSocket.accept[JsValue, JsValue] { _ =>
    ActorFlow.actorRef { out =>
        SocketActor.props(out, authenticator, gameManager)
    }
  }
}
