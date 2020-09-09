package controllers

import dao.UserDao
import javax.inject.Inject
import play.api.mvc._
import services.Jwt

import scala.concurrent.{ExecutionContext, Future}

class AuthenticatedAction @Inject()(parser: BodyParsers.Default, jwt: Jwt, userDao: UserDao)(implicit ec: ExecutionContext)
  extends ActionBuilder[UserRequest, AnyContent] {

  override def parser: BodyParser[AnyContent] = parser

  override protected def executionContext: ExecutionContext = ec

  override def invokeBlock[A](request: Request[A], block: UserRequest[A] => Future[Result]): Future[Result] = {
    val userRequest: Future[Option[UserRequest[A]]] =
      jwt.getUserId(request).map(id =>
        userDao.findById(id).map(UserRequest.validate(_, request))
      ).getOrElse(Future.successful(None))
    userRequest.flatMap {
      case Some(request) => block(request)
      case None => Future.successful(Results.Unauthorized(""))
    }
  }
}
