package models.game

import play.api.libs.json.{Format, JsError, JsPath, JsValue, Json, Reads}

sealed trait Message

case class Authentication(token: String) extends Message

object Authentication {
  implicit val authenticationFormat: Format[Authentication] = Json.format
}

case class ConnectToGame(gameId: Int) extends Message
object ConnectToGame {
  implicit val connectFormat: Format[ConnectToGame] = Json.format
}

object Message {
  implicit val messageReads: Reads[Message] = (js: JsValue) => {
    val messageType = (JsPath \ "messageType").read[String].reads(js)
    messageType.fold(
      _ => JsError("stage undefined or incorrect"), {
        case "Authentication" =>
          (JsPath \ "data").read[Authentication].reads(js)
        case "ConnectToGame" =>
          (JsPath \ "data").read[ConnectToGame].reads(js)
      }
    )
  }
}

case object Village extends Message
case object JoinRequest extends Message
case object LeaveGame extends Message