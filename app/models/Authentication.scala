package models

import play.api.libs.json.{Json, OFormat, OWrites}

case class Authentication(user: User, token: String)

object Authentication {
  implicit val authFormat: OFormat[Authentication] = Json.format[Authentication]
  implicit val authWrites: OWrites[Authentication] = Json.writes[Authentication]
}
