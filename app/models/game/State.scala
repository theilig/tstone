package models.game

import controllers.game.stage.{GameStage, WaitingForPlayers}
import models.schema.Tables.GameRow
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class State(players: List[Player], village: Option[Village], dungeon: Option[Dungeon], currentStage: GameStage) {
  def ownerId: Int = players.filterNot(_.pending).headOption.map(_.userId).getOrElse(0)

}

object State {
  implicit val stateFormat: Format[State] = Json.format[State]

  def apply(gameOwner: Player): State = {
    val players = List(gameOwner)
    new State(players, None, None, WaitingForPlayers)
  }

  def apply(gameRow: GameRow): State = {
    Json.parse(gameRow.state).as[State]
  }
}

