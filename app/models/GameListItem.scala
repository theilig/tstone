package models

import models.schema.Tables.GameRow
import play.api.libs.json._

case class GameListItem(
                         gameId: Int,
                         state: JsValue
                       )

object GameListItem {
  implicit val gameListItemFormat: OFormat[GameListItem] = Json.format[GameListItem]
  implicit val gameListItemWrites: OWrites[GameListItem] = Json.writes[GameListItem]

  def apply(gameRow: GameRow): GameListItem = {
    new GameListItem(gameRow.gameId, Json.parse(gameRow.state))
  }
}
