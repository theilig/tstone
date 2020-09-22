package models.schema
// AUTO-GENERATED Slick data model for table Thunderstone
trait ThunderstoneTable {

  self:Tables  =>

  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}
  /** Entity class storing rows of table Thunderstone
   *  @param cardId Database column card_id SqlType(INT), PrimaryKey
   *  @param victoryPoints Database column victory_points SqlType(INT) */
  case class ThunderstoneRow(cardId: Int, victoryPoints: Int)
  /** GetResult implicit for fetching ThunderstoneRow objects using plain SQL queries */
  implicit def GetResultThunderstoneRow(implicit e0: GR[Int]): GR[ThunderstoneRow] = GR{
    prs => import prs._
    ThunderstoneRow.tupled((<<[Int], <<[Int]))
  }
  /** Table description of table Thunderstone. Objects of this class serve as prototypes for rows in queries. */
  class Thunderstone(_tableTag: Tag) extends profile.api.Table[ThunderstoneRow](_tableTag, Some("TStone"), "Thunderstone") {
    def * = (cardId, victoryPoints) <> (ThunderstoneRow.tupled, ThunderstoneRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(cardId), Rep.Some(victoryPoints))).shaped.<>({r=>import r._; _1.map(_=> ThunderstoneRow.tupled((_1.get, _2.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column card_id SqlType(INT), PrimaryKey */
    val cardId: Rep[Int] = column[Int]("card_id", O.PrimaryKey)
    /** Database column victory_points SqlType(INT) */
    val victoryPoints: Rep[Int] = column[Int]("victory_points")
  }
  /** Collection-like TableQuery object for table Thunderstone */
  lazy val Thunderstone = new TableQuery(tag => new Thunderstone(tag))
}
