package dao

import javax.inject.Inject
import models.game.State
import models.{GameListItem, User}
import models.schema.Tables
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class GameDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
                        (implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  private val Games = TableQuery[Tables.Game]

  private val Users = TableQuery[Tables.User]

  private val createGameQuery = Games returning Games.map(_.gameId) into
    ((game, gameId) => game.copy(gameId = gameId))


  def gameList: Future[List[GameListItem]] = {
    val allGames = for {
      ((game, roadManager), homeManager) <- (Games join Users on (_.roadManager === _.userId)) join Users on (_._1.homeManager === _.userId)
    } yield (game, roadManager.firstName, homeManager.firstName)
    db.run(allGames.result).map { list =>
        list.map {
          case (gameRow, road, home) => GameListItem(gameRow)
        }
    }.map(x => x.toList)
  }
  def insertGame(user: User, state: State): Future[Tables.GameRow] = {
    val newGame = Tables.GameRow(0, completed = false, Some(Json.toJson(state).toString), user.userId, user.userId)
    db.run(createGameQuery += newGame).map(row => {
      row
    })
  }

  def findById(gameId: Int): Future[Option[Tables.GameRow]] = {
    db.run(Games.filter(_.gameId === gameId).result.headOption)
  }

}
