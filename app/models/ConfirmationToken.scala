package models

import play.api.libs.json.{Json, OFormat, OWrites}

case class ConfirmationToken(token: String)

object ConfirmationToken {
  implicit val tokenFormat: OFormat[ConfirmationToken] = Json.format[ConfirmationToken]
  implicit val tokenWrites: OWrites[ConfirmationToken] = Json.writes[ConfirmationToken]

}
