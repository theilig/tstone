package models

import models.schema.Tables.UserRow
import play.api.libs.json.{Json, OFormat}

case class User(userId: Int, firstName: String, lastName: String, email: String)

object User {
  implicit val userFormat: OFormat[User] = Json.format[User]
  def apply(userRow: UserRow): User = {
    new User(userRow.userId, userRow.firstName, userRow.lastName, userRow.email)
  }
}
