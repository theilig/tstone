package models.schema
// AUTO-GENERATED Slick data model for table Game
trait GameTable {

  self:Tables  =>

  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}
  /** Entity class storing rows of table Game
   *  @param gameId Database column game_id SqlType(INT), AutoInc, PrimaryKey
   *  @param state Database column state SqlType(TEXT)
   *  @param completed Database column completed SqlType(BIT), Default(false) */
  case class GameRow(gameId: Int, state: String, completed: Boolean = false)
  /** GetResult implicit for fetching GameRow objects using plain SQL queries */
  implicit def GetResultGameRow(implicit e0: GR[Int], e1: GR[String], e2: GR[Boolean]): GR[GameRow] = GR{
    prs => import prs._
    GameRow.tupled((<<[Int], <<[String], <<[Boolean]))
  }
  /** Table description of table Game. Objects of this class serve as prototypes for rows in queries. */
  class Game(_tableTag: Tag) extends profile.api.Table[GameRow](_tableTag, Some("TStone"), "Game") {
    def * = (gameId, state, completed) <> (GameRow.tupled, GameRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(gameId), Rep.Some(state), Rep.Some(completed))).shaped.<>({r=>import r._; _1.map(_=> GameRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column game_id SqlType(INT), AutoInc, PrimaryKey */
    val gameId: Rep[Int] = column[Int]("game_id", O.AutoInc, O.PrimaryKey)
    /** Database column state SqlType(TEXT) */
    val state: Rep[String] = column[String]("state")
    /** Database column completed SqlType(BIT), Default(false) */
    val completed: Rep[Boolean] = column[Boolean]("completed", O.Default(false))

    /** Index over (completed) (database name completed) */
    val index1 = index("completed", completed)
  }
  /** Collection-like TableQuery object for table Game */
  lazy val Game = new TableQuery(tag => new Game(tag))
}
