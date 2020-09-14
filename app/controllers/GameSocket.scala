package controllers
import actors.SocketActor
import play.api.mvc._
import play.api.libs.streams.ActorFlow
import javax.inject.Inject
import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.libs.json.JsValue
import services.Authenticator

import scala.concurrent.ExecutionContext

class GameSocket @Inject()(cc: ControllerComponents, authenticator: Authenticator)
                          (implicit system: ActorSystem, mat: Materializer, ec: ExecutionContext)
  extends AbstractController(cc) {
  def socket: WebSocket = WebSocket.accept[JsValue, JsValue] { _ =>
    ActorFlow.actorRef { out =>
        SocketActor.props(out, authenticator)
    }
  }
}
