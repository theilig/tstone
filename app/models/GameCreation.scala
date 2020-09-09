package models

import play.api.libs.json.{Json, OFormat}

case class GameCreation(players: List[Int])

object GameCreation {
  implicit val gameCreationFormat: OFormat[GameCreation] = Json.format[GameCreation]

}
