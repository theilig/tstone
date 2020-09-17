package models.game

import controllers.game.stage.{GameStage, WaitingForPlayers}
import models.Player
import models.schema.Tables.GameRow
import play.api.libs.json._

case class State(players: List[Player], village: List[List[VillageCard]], monsters: List[DungeonCard], currentStage: GameStage) {
  def ownerId: Int = players.filterNot(_.pending).headOption.map(_.userId).getOrElse(0)

}

object State {
  implicit val stateReads: Reads[State] = (json: JsValue) => {
    JsSuccess(State(
      (json \ "players").as[List[Player]],
      (json \ "village").as[List[List[VillageCard]]],
      (json \ "monsters").as[List[DungeonCard]],
      (json \ "currentStage").as[GameStage]
    ))
  }

  implicit val stateFormat: Writes[State] = (state: State) => JsObject(
    Seq(
      "players" -> Json.toJson(state.players),
      "village" -> Json.toJson(state.village),
      "monsters" -> Json.toJson(state.monsters),
      "currentStage" -> Json.toJson(state.currentStage)
    )
  )

  def apply(gameOwner: Player): State = {
    val players = List(gameOwner)
    new State(players, Nil, Nil, WaitingForPlayers)
  }

  def apply(gameRow: GameRow): State = {
    Json.parse(gameRow.state).as[State]
  }
}

