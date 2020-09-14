package services

import dao.UserDao
import javax.inject.Inject
import models.User
import models.schema.Tables.UserRow
import play.api.mvc.Headers

import scala.concurrent.{ExecutionContext, Future}

class Authenticator @Inject() (jwt: Jwt, userDao: UserDao)(implicit ec: ExecutionContext) {

  def validate(possibleUser: Option[UserRow]): Option[User] = {
    possibleUser match {
      case Some(userRow) if userRow.confirmed => Some(User(userRow))
      case _ => None
    }
  }

  def authenticate(jwtToken: String): Future[Option[User]] = {
    jwt.decodePayload(jwtToken).map(userId =>
      userDao.findById(userId)).getOrElse(Future.successful(None)).map(validate)
  }

  def authenticate(headers: Headers): Future[Option[User]] = {
    val HeaderTokenRegex = """Bearer\s+(.+?)""".r

    val authHeader = headers.get("Authorization").getOrElse("")
    val jwtToken = authHeader match {
      case HeaderTokenRegex(token) => token
      case _ => ""
    }
    authenticate(jwtToken)
  }
}
