package controllers

import controllers.game.stage._
import controllers.game._
import javax.inject.Inject
import models.game.State
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

class GameController @Inject()(
                                cc: ControllerComponents,
                                gameAction: GameAction,
                              )(implicit executionContext: ExecutionContext)
  extends AbstractController(cc) {

  private def currentStageController(game: State): GameStage = {
    game.stage match {
      case "PickDestination" => new PickDestination
    }
  }

  def setGame(resource: String = ""): Action[AnyContent] =
    gameAction.async { implicit request: GameRequest[AnyContent] =>
      try {
        val stage = currentStageController(request.game)
        val message = resource match {
          case "Village" =>
            Village
        }
        stage.receive(message, request.userRequest.user, request.game)
      } catch {
        case _: MatchError => Future.successful(BadRequest("Unexpected message"))
      }
    }

  def getGame: Action[AnyContent] =
    gameAction.async { implicit request: GameRequest[AnyContent] =>
      try {
        Future.successful(Ok(Json.toJson(request.game)))
      } catch {
        case _: MatchError => Future.successful(BadRequest("Unexpected message"))
      }
    }
}
