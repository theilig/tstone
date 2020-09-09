package controllers

import models.User
import models.schema.Tables.UserRow
import play.api.mvc.{Request, WrappedRequest}

case class UserRequest[A](user: User, request: Request[A]) extends WrappedRequest[A](request)

object UserRequest {
  def validate[A](possibleUserRow: Option[UserRow], request: Request[A]): Option[UserRequest[A]] = {
    possibleUserRow match {
        case Some(row) if row.confirmed => Some(UserRequest(models.User(row), request))
        case _ => None
    }
  }
}
