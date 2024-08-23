package dao

import javax.inject.Inject
import models.schema.Tables
import models.{ConfirmationToken, LoginAttempt, SignupInformation}
import org.mindrot.jbcrypt.BCrypt
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import services.Jwt
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class UserDao @Inject() (
                          jwt: Jwt,
                          protected val dbConfigProvider: DatabaseConfigProvider
                        )(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  private val Users = TableQuery[Tables.User]

  private val UserConfirmations = TableQuery[Tables.UserConfirmation]

  private val insertConfirmationQuery = UserConfirmations returning UserConfirmations.map(_.userConfirmationId) into
    ((userConfirmation, userConfirmationId) => userConfirmation.copy(userConfirmationId = userConfirmationId))

  private val createUserQuery = Users returning Users.map(_.userId) into
    ((user, userId) => user.copy(userId = userId))

  def addUser(signupInformation: SignupInformation): Future[Tables.UserConfirmationRow] = {
    val HashLogRounds = 12
    val newUser = Tables.UserRow(
      0,
      signupInformation.firstName,
      signupInformation.lastName,
      BCrypt.hashpw(signupInformation.password, BCrypt.gensalt(HashLogRounds)),
      signupInformation.email
    )
    db.run(createUserQuery += newUser).flatMap(row => {
      createConfirmation(row.userId)
    })
  }

  private def createConfirmation(userId: Int): Future[Tables.UserConfirmationRow] = {
    val ConfirmationLength = 8
    val confirmation = Tables.UserConfirmationRow(userConfirmationId = 0, token = Random.alphanumeric.take(ConfirmationLength).mkString, userId = userId)
    db.run(insertConfirmationQuery += confirmation)
  }

  def getConfirmation(userId: Int): Future[Tables.UserConfirmationRow] = {
    val existingConfirmation: Future[Option[Tables.UserConfirmationRow]] =
      db.run(UserConfirmations.filter(_.userId === userId).result.headOption)
    existingConfirmation.flatMap {
      case Some(existing) => Future.successful(existing)
      case None => createConfirmation(userId)
    }
  }

  def findByEmail(email: String): Future[Option[Tables.UserRow]] = {
    db.run(Users.filter(_.email === email).result.headOption)
  }

  def findById(userId: Int): Future[Option[Tables.UserRow]] = {
    db.run(Users.filter(_.userId === userId).result.headOption)
  }


  private def findUserForConfirmationToken(token: ConfirmationToken): Future[Option[Tables.UserConfirmationRow]] = {
    db.run(UserConfirmations.filter(_.token === token.token).result.headOption)
  }

  def confirm(confirmationToken: ConfirmationToken): Future[Option[(Tables.UserRow, String)]] = {
    findUserForConfirmationToken(confirmationToken).flatMap {
      case Some(row) =>
        db.run(UserConfirmations.filter(_.userConfirmationId === row.userConfirmationId).delete)
        db.run(Users.filter(_.userId === row.userId).map(_.confirmed).update(true))
        findById(row.userId).map({
          case Some(userRow) => Some(userRow, jwt.createToken(userRow.userId))
          case None => None
        })
      case None => Future.successful(None)
    }
  }

  def login(loginAttempt: LoginAttempt, userRow: Tables.UserRow): Option[String] = {
    if (BCrypt.checkpw(loginAttempt.password, userRow.passwordHash)) {
      Some(jwt.createToken(userRow.userId))
    } else {
      None
    }
  }

  def findPlayers(players: List[Int]): Future[List[Tables.UserRow]] = {
    db.run(Users.filter(u => u.userId.inSet(players.toSet)).result).map(_.toList)
  }

}
