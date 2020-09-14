package models.schema
// AUTO-GENERATED Slick data model for table GamePlayers
trait GamePlayersTable {

  self:Tables  =>

  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}
  /** Entity class storing rows of table GamePlayers
   *  @param gamePlayerId Database column game_player_id SqlType(INT), AutoInc, PrimaryKey
   *  @param gameId Database column game_id SqlType(INT)
   *  @param userId Database column user_id SqlType(INT) */
  case class GamePlayersRow(gamePlayerId: Int, gameId: Int, userId: Int)
  /** GetResult implicit for fetching GamePlayersRow objects using plain SQL queries */
  implicit def GetResultGamePlayersRow(implicit e0: GR[Int]): GR[GamePlayersRow] = GR{
    prs => import prs._
    GamePlayersRow.tupled((<<[Int], <<[Int], <<[Int]))
  }
  /** Table description of table GamePlayers. Objects of this class serve as prototypes for rows in queries. */
  //noinspection ScalaUnnecessaryParentheses
  class GamePlayers(_tableTag: Tag) extends profile.api.Table[GamePlayersRow](_tableTag, Some("TStone"), "GamePlayers") {
    def * = (gamePlayerId, gameId, userId) <> (GamePlayersRow.tupled, GamePlayersRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(gamePlayerId), Rep.Some(gameId), Rep.Some(userId))).shaped.<>({r=>import r._; _1.map(_=> GamePlayersRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column game_player_id SqlType(INT), AutoInc, PrimaryKey */
    val gamePlayerId: Rep[Int] = column[Int]("game_player_id", O.AutoInc, O.PrimaryKey)
    /** Database column game_id SqlType(INT) */
    val gameId: Rep[Int] = column[Int]("game_id")
    /** Database column user_id SqlType(INT) */
    val userId: Rep[Int] = column[Int]("user_id")

    /** Uniqueness Index over (gameId,userId) (database name game_player) */
    val index1 = index("game_player", (gameId, userId), unique=true)
  }
  /** Collection-like TableQuery object for table GamePlayers */
  lazy val GamePlayers = new TableQuery(tag => new GamePlayers(tag))
}
