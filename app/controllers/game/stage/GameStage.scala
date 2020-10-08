package controllers.game.stage

import models.User
import models.game.{GameError, Message, Player, State}
import play.api.libs.json.{Format, JsError, JsObject, JsPath, JsString, JsSuccess, Reads, Writes}

abstract class GameStage {
  def receive(message: Message, user: User, state: State): Either[GameError, State]
  def canAddPlayers: Boolean = false
  def currentPlayer(state: State): Option[Player]
}

object GameStage {
  implicit val format: Format[GameStage] = Format[GameStage](
    Reads { js =>
      val stage = (JsPath \ "stage").read[String].reads(js)
      stage.fold(
        _ => JsError("stage undefined or incorrect"), {
          case "WaitingForPlayers" => JsSuccess(WaitingForPlayers)
          case "ChoosingDestination" => (JsPath \ "data").read[ChoosingDestination].reads(js)
          case "Resting" => (JsPath \ "data").read[Resting].reads(js)
          case "Purchasing" => (JsPath \ "data").read[Purchasing].reads(js)
        }
      )
    },
    Writes {
      case WaitingForPlayers => JsObject(Seq("stage" -> JsString("WaitingForPlayers")))
      case c: ChoosingDestination => JsObject(
        Seq(
          "stage" -> JsString("ChoosingDestination"),
          "data" -> ChoosingDestination.format.writes(c)
        )
      )
      case r: Resting => JsObject(
        Seq(
          "stage" -> JsString("Resting"),
          "data" -> Resting.format.writes(r)
        )
      )
      case p: Purchasing => JsObject(
        Seq(
          "stage" -> JsString("Purchasing"),
          "data" -> Purchasing.format.writes(p)
        )
      )
    }
  )
}

