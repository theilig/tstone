package controllers

import dao.GameDao
import javax.inject.Inject
import models.game.{Player, State}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

class SetupController @Inject()(
                               gameDao: GameDao,
                               cc: ControllerComponents,
                               authenticatedAction: AuthenticatedAction
                               )
                               (implicit executionContext: ExecutionContext)
    extends AbstractController(cc) {
  def showGameList(): Action[AnyContent] = authenticatedAction.async { implicit request: UserRequest[AnyContent] =>
    gameDao.gameList.map(list => Ok(Json.toJson(Map("games" -> list))))
  }

  def newGame(): Action[AnyContent] = authenticatedAction.async { implicit request: UserRequest[AnyContent] =>
    try {
        gameDao.insertGame(State(Player(request.user, pending = false))).map(row =>
          Ok(Json.toJson(models.GameListItem(row))))
    } catch {
      case _: Throwable =>
        Future.successful(BadRequest("Invalid Json"))
    }
  }
}