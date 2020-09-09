package models

import play.api.libs.json.{Json, OFormat}

case class UserConfirmation(token: String, userId: Int)

object UserConfirmation {
  implicit val userConfirmationFormat: OFormat[UserConfirmation] = Json.format[UserConfirmation]
}
