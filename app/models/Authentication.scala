package models

import play.api.libs.json.{Json, OFormat}

case class Authentication(user: User, token: String)

object Authentication {
  implicit val authFormat: OFormat[Authentication] = Json.format[Authentication]
}
