package models
import models.game.Card
import play.api.libs.json.{Json, OFormat, OWrites}
import schema.Tables.UserRow

case class Player(userId: Int,
                  name: String,
                  pending: Boolean,
                  discard: List[Card],
                  hand: List[Card],
                  deck: List[Card],
                  xp: Int
                 )

object Player {
  implicit val playerFormat: OFormat[Player] = Json.format[Player]
  implicit val playerWrites: OWrites[Player] = Json.writes[Player]

  def apply(userId: Int, firstName: String, lastName: String, pending: Boolean): Player = {
    new Player(userId, s"$firstName ${lastName.head}.", pending, Nil, Nil, Nil, 0)
  }
  def apply(userRow: UserRow, pending: Boolean): Player = {
    apply(userRow.userId, userRow.firstName, userRow.lastName, pending)
  }

  def apply(user: User, pending: Boolean): Player = {
    apply(user.userId, user.firstName, user.lastName, pending)
  }
}
