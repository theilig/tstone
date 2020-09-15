package models.game

import play.api.libs.json.{Format, JsError, JsObject, JsPath, JsResult, JsString, JsSuccess, JsValue, Json, Reads, Writes}

sealed trait Message

case class Authentication(token: String) extends Message

object Authentication {
  implicit val authenticationFormat: Format[Authentication] = Json.format
}

case class ConnectToGame(gameId: Int) extends Message
object ConnectToGame {
  implicit val connectFormat: Format[ConnectToGame] = Json.format
}

case class GameState(state: State) extends Message
object GameState {
  implicit val gameStateFormat: Format[GameState] = Json.format
}

case class GameError(message: String) extends Message
object GameError {
  implicit val errorFormat: Format[GameError] = Json.format
}

case object GameOver extends Message

case object LeaveGame extends Message

object Message {
  implicit val messageFormat: Format[Message] = Format[Message](
    Reads { js =>
      val messageType: JsResult[String] = (JsPath \ "messageType").read[String].reads(js)
      messageType.fold(
        _ => JsError("stage undefined or incorrect"), {
          case "Authentication" =>
            (JsPath \ "data").read[Authentication].reads(js)
          case "ConnectToGame" =>
            (JsPath \ "data").read[ConnectToGame].reads(js)
          case "LeaveGame" => JsSuccess(LeaveGame)
        }
      )
    },
    Writes {
      case e: GameError =>
        JsObject(
          Seq(
            "messageType" -> JsString("Error"),
            "data" -> GameError.errorFormat.writes(e)
          )
        )
      case gs: GameState =>
        JsObject(
          Seq(
            "messageType" -> JsString("GameState"),
            "data" -> GameState.gameStateFormat.writes(gs)
          )
        )
      case m =>
        JsObject(
          Seq(
            "messageType" -> JsString(m.getClass.getSimpleName)
          )
        )
    }
  )
}

case object Village extends Message
case object JoinRequest extends Message
