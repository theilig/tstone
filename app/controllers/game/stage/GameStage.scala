package controllers.game.stage

import models.User
import models.game.{GameError, Message, State}
import play.api.libs.json.{Format, JsError, JsObject, JsPath, JsString, JsSuccess, Reads, Writes}

abstract class GameStage {
  def receive(message: Message, user: User, state: State): Either[State, GameError]
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

