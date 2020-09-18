package models.game

import controllers.game.stage.{GameStage, WaitingForPlayers}
import models.schema.Tables.GameRow
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class State(players: List[Player], village: Option[Village], dungeon: Option[Dungeon], currentStage: GameStage) {
  def ownerId: Int = players.filterNot(_.pending).headOption.map(_.userId).getOrElse(0)

}

object State {
  implicit val stateReads: Reads[State] = (
    (JsPath \ "players").read[List[Player]] and
      (JsPath \ "village").readNullable[Village] and
      (JsPath \ "dungeon").readNullable[Dungeon] and
      (JsPath \ "currentStage").read[GameStage]
    )(State.apply _)

  implicit val stateFormat: Writes[State] = (state: State) => JsObject(
    Seq(
      "players" -> Json.toJson(state.players),
      "village" -> Json.toJson(state.village),
      "dungeon" -> Json.toJson(state.dungeon),
      "currentStage" -> Json.toJson(state.currentStage)
    )
  )

  def apply(gameOwner: Player): State = {
    val players = List(gameOwner)
    new State(players, None, None, WaitingForPlayers)
  }

  def apply(gameRow: GameRow): State = {
    Json.parse(gameRow.state).as[State]
  }
}

