package controllers

import dao.UserDao
import javax.inject.Inject
import models.{ConfirmationToken, LoginAttempt, SignupInformation, schema}
import play.api.libs.json.Json
import play.api.mvc._
import services.Mailer

import scala.concurrent.{ExecutionContext, Future}

class LoginController @Inject()(
                                 userDao: UserDao,
                                 mailer: Mailer,
                                 controllerComponents: ControllerComponents
                               )(implicit executionContext: ExecutionContext)
                               extends AbstractController(controllerComponents) {

  private def sendConfirmation(email: String, eventualRow: Future[schema.Tables.UserConfirmationRow]): Future[String] = {
    eventualRow.map(row => {
      mailer.sendConfirmation(email, row.token)
    })
  }

  def processLoginAttempt: Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    try {
      val loginAttempt = request.body.asJson.get.as[LoginAttempt]
      userDao.findByEmail(loginAttempt.email).flatMap {
        case Some(userRow) if userRow.confirmed => Future.successful(
          userDao.login(loginAttempt, userRow).map(token => {
            Ok(Json.toJson(models.Authentication(models.User(userRow), token)))
          }).getOrElse(BadRequest("Unable to validate user or password"))
        )
        case Some(userRow) =>
            sendConfirmation(userRow.email, userDao.getConfirmation(userRow.userId)).map(_ => {
              BadRequest("Account not confirmed, confirmation has been resent")
            }).recoverWith({
              case _ : Throwable => Future.successful(BadRequest("Account not confirmed, error resending confirmation"))
            })
        case None => Future.successful(BadRequest("Unable to validate user or password"))
      }
    } catch {
      case _: Throwable => Future.successful(BadRequest("Invalid Json"))
    }
  }

  def processSignupAttempt: Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    try {
      val signupInformation = request.body.asJson.get.as[SignupInformation]
      userDao.findByEmail(signupInformation.email).flatMap {
        case Some(row) if row.confirmed => Future.successful(BadRequest("This email already has an account"))
        case Some(row) =>
          sendConfirmation(row.email, userDao.getConfirmation(row.userId)).map(_ =>
            BadRequest("This email has already been used, resending confirmation")).recoverWith {
            case _: Throwable => Future.successful(
              BadRequest("This email has already been used, error resending confirmation")
            )
          }
        case None =>
          sendConfirmation(
            signupInformation.email,
            userDao.addUser(signupInformation)
          ).map(_ =>
            Ok(s"A confirmation code has been sent to ${signupInformation.email}")
          ).recover(_ => {
            BadRequest(
              """Account was created but confirmation email could not be sent.
                |You can log into your account and the email will be attempted again""".stripMargin
            )
          })
      }
    } catch {
      case _ : Throwable => Future.successful(BadRequest("Invalid Json"))
    }
  }

  def processLogout: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Redirect("showLogin").withNewSession
  }

  def processConfirmation: Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    try {
      val token = request.body.asJson.get.as[ConfirmationToken]
      userDao.confirm(token).map({
        case Some((userRow, jwtToken)) => Ok(Json.toJson(models.Authentication(models.User(userRow), jwtToken)))
        case None => BadRequest("Invalid Token")
      })
    } catch {
      case _: Throwable => Future.successful(BadRequest("Invalid Json"))
    }
  }
}
