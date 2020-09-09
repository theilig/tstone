package dao

import javax.inject.Inject
import models.game.State
import models.GameListItem
import models.schema.Tables
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.json.Json
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class GameDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
                        (implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  private val Games = TableQuery[Tables.Game]

  private val createGameQuery = Games returning Games.map(_.gameId) into
    ((game, gameId) => game.copy(gameId = gameId))


  def gameList: Future[List[GameListItem]] = {
    val allGames = db.run(Games.result)
    allGames.map (list => {
      list.map(game => {
        GameListItem(game.gameId, Json.parse(game.state))
      })
    }).map(x => x.toList)
  }

  def insertGame(state: State): Future[Tables.GameRow] = {
    val newGame = Tables.GameRow(0, state = Json.toJson(state).toString)
    db.run(createGameQuery += newGame).map(row => {
      row
    })
  }

  def findById(gameId: Int): Future[Option[Tables.GameRow]] = {
    db.run(Games.filter(_.gameId === gameId).result.headOption)
  }

}
