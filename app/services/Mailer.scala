package services

import javax.inject.Inject
import play.api.libs.mailer
import play.api.libs.mailer.MailerClient

class Mailer @Inject() (client: MailerClient) {
  def sendConfirmation(email: String, token: String): String = {
    val confirmationEmail = mailer.Email(
      "Please confirm your e-mail address",
      "no-reply@heilig.com",
      Seq(email),
      Some(s"Please follow the link below to confirm your email\nhttps://tstone.heilig.com/confirm/$token")
    )
    client.send(confirmationEmail)
  }
}
