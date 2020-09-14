package models.game

import controllers.game.stage.{WaitingForPlayers, GameStage}
import models.Player
import models.schema.Tables.GameRow
import play.api.libs.json._

case class State(ownerId: Int, players: List[Player], currentStage: GameStage)

object State {
  implicit val stateReads: Reads[State] = (json: JsValue) => {
    JsSuccess(State(
      (json \ "ownerId").as[Int],
      (json \ "players").as[List[Player]],
      (json \ "currentStage").as[GameStage]
    ))
  }

  implicit val stateFormat: Writes[State] = (state: State) => JsObject(
    Seq(
      "ownerId" -> Json.toJson(state.ownerId),
      "players" -> Json.toJson(state.players),
      "currentStage" -> Json.toJson(state.currentStage)
    )
  )

  def apply(gameOwner: Player): State = {
    val players = List(gameOwner)
    new State(gameOwner.userId, players, WaitingForPlayers)
  }

  def apply(gameRow: GameRow): State = {
    Json.parse(gameRow.state).as[State]
  }
}

