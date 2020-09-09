package models

import play.api.libs.json.{Json, OFormat}

case class SignupInformation(firstName: String, lastName: String, email: String, password: String)

object SignupInformation {
  implicit val signupInformationFormat: OFormat[SignupInformation] = Json.format[SignupInformation]
}
