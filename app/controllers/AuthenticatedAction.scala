package controllers

import javax.inject.Inject
import play.api.mvc._
import services.Authenticator

import scala.concurrent.{ExecutionContext, Future}

class AuthenticatedAction @Inject()(parser: BodyParsers.Default, authenticator: Authenticator)(implicit ec: ExecutionContext)
  extends ActionBuilder[UserRequest, AnyContent] {

  override def parser: BodyParser[AnyContent] = parser

  override protected def executionContext: ExecutionContext = ec

  override def invokeBlock[A](request: Request[A], block: UserRequest[A] => Future[Result]): Future[Result] = {
    val possibleUserRequest: Future[Option[UserRequest[A]]] = {
      val eventualPossibleUser = authenticator.authenticate(request.headers)
      eventualPossibleUser.map{
         case Some(user) => Some(UserRequest(user, request))
         case None => None
       }
    }
    possibleUserRequest.flatMap {
      case Some(request) => block(request)
      case None => Future.successful(Results.Unauthorized(""))
    }
  }
}
