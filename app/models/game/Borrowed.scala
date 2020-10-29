package models.game

import play.api.libs.json.{Format, Json}

case class Borrowed(userId: Int, card: Card)

object Borrowed {
  implicit val BorrowedFormat: Format[Borrowed] = Json.format
}
