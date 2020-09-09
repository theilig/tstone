package models

import models.schema.Tables.UserRow
import play.api.libs.json.{Json, OFormat}

case class User(email: String, userId: Int, firstName: String)

object User {
  implicit val userFormat: OFormat[User] = Json.format[User]
  def apply(userRow: UserRow): User = {
    new User(userRow.email, userRow.userId, userRow.firstName)
  }
}
