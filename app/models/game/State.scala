package models.game

import models.schema.Tables.{GameRow, UserRow}
import play.api.libs.json._

import scala.concurrent.ExecutionContext
import scala.util.Random

case class State(ownerId: Int, players: List[Int], currentPlayer: Int, stage: String)

object State {
  implicit val stateReads: Reads[State] = (json: JsValue) => {
    JsSuccess(State(
      (json \ "ownerId").as[Int],
      (json \ "players").as[List[Int]],
      (json \ "currentPlayer").as[Int],
      (json \ "stage").as[String]
    ))
  }

  implicit val stateFormat: Writes[State] = (state: State) => JsObject(
    Seq(
      "ownerId" -> Json.toJson(state.ownerId),
      "players" -> Json.toJson(state.players),
      "currentPlayer" -> Json.toJson(state.currentPlayer),
      "stage" -> JsString(state.stage)
    )
  )

  def apply(
             gameOwnerId: Int,
             players: List[UserRow]
           )(implicit executionContext: ExecutionContext): State = {
    val playerIds = players.map(_.userId)
    val state = new State(
      gameOwnerId,
      playerIds,
      playerIds.drop(new Random().nextInt(playerIds.length)).head,
      "PickDestination"
    )
    state
  }

  def apply(gameRow: GameRow): State = {
    Json.parse(gameRow.state).as[State]
  }
}
