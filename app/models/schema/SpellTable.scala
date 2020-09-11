package models.schema
// AUTO-GENERATED Slick data model for table Spell
trait SpellTable {

  self:Tables  =>

  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}
  /** Entity class storing rows of table Spell
   *  @param cardId Database column card_id SqlType(INT), PrimaryKey
   *  @param light Database column light SqlType(INT)
   *  @param cost Database column cost SqlType(INT)
   *  @param victoryPoints Database column victory_points SqlType(INT) */
  case class SpellRow(cardId: Int, light: Int, cost: Int, victoryPoints: Int)
  /** GetResult implicit for fetching SpellRow objects using plain SQL queries */
  implicit def GetResultSpellRow(implicit e0: GR[Int]): GR[SpellRow] = GR{
    prs => import prs._
    SpellRow.tupled((<<[Int], <<[Int], <<[Int], <<[Int]))
  }
  /** Table description of table Spell. Objects of this class serve as prototypes for rows in queries. */
  class Spell(_tableTag: Tag) extends profile.api.Table[SpellRow](_tableTag, Some("TStone"), "Spell") {
    def * = (cardId, light, cost, victoryPoints) <> (SpellRow.tupled, SpellRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(cardId), Rep.Some(light), Rep.Some(cost), Rep.Some(victoryPoints))).shaped.<>({r=>import r._; _1.map(_=> SpellRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column card_id SqlType(INT), PrimaryKey */
    val cardId: Rep[Int] = column[Int]("card_id", O.PrimaryKey)
    /** Database column light SqlType(INT) */
    val light: Rep[Int] = column[Int]("light")
    /** Database column cost SqlType(INT) */
    val cost: Rep[Int] = column[Int]("cost")
    /** Database column victory_points SqlType(INT) */
    val victoryPoints: Rep[Int] = column[Int]("victory_points")
  }
  /** Collection-like TableQuery object for table Spell */
  lazy val Spell = new TableQuery(tag => new Spell(tag))
}
