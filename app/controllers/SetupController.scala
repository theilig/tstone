package controllers

import dao.{GameDao, UserDao}
import javax.inject.Inject
import models.game.State
import models.GameCreation
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

class SetupController @Inject()(
                               gameDao: GameDao,
                               userDao: UserDao,
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
      val gameCreation = request.body.asJson.get.as[GameCreation]
      val eventualPlayers = userDao.findPlayers(gameCreation.players)
      val currentUser = request.user
      eventualPlayers.map(players => {
        State(request.user.userId, players)
      }).flatMap(state => {
        gameDao.insertGame(currentUser, state)
      }).map(row => Ok(Json.toJson(models.GameListItem(row))))
    } catch {
      case _: Throwable =>
        Future.successful(BadRequest("Invalid Json"))
    }
  }
}