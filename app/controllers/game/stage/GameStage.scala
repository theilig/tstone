package controllers.game.stage

import dao.GameDao
import models.User
import models.game.{Message, State}
import play.api.libs.json.{Format, JsError, JsObject, JsPath, JsString, JsSuccess, Reads, Writes}
import play.api.mvc.Result

import scala.concurrent.{ExecutionContext, Future}

abstract class GameStage {
  def receive(message: Message, user: User, gameId: Int, state: State, gameDao: GameDao)
             (implicit executionContext: ExecutionContext): Future[Result]
  def canAddPlayers: Boolean = false
}

object GameStage {
  implicit val format: Format[GameStage] = Format[GameStage](
    Reads { js =>
      val stage = (JsPath \ "stage").read[String].reads(js)
      stage.fold(
        _ => JsError("stage undefined or incorrect"), {
          case "WaitingForPlayers" =>
            JsSuccess(WaitingForPlayers)
          case "ChoosingDestination"  =>
            (JsPath \ "data").read[PickDestination].reads(js)
        }
      )
    },
    Writes {
      case WaitingForPlayers => JsObject(Seq("stage" -> JsString("WaitingForPlayers")))
      case p: PickDestination => JsObject(
        Seq(
          "stage" -> JsString("ChoosingDestination"),
          "data" -> PickDestination.format.writes(p)
        )
      )
    }
  )
}

