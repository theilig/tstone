package controllers.game

import controllers.UserRequest
import models.game.State
import play.api.mvc.WrappedRequest

case class GameRequest[A](game: State, userRequest: UserRequest[A]) extends WrappedRequest[A](userRequest)
