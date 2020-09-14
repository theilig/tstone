package models.schema
// AUTO-GENERATED Slick data model for table Item
trait ItemTable {

  self:Tables  =>

  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}
  /** Entity class storing rows of table Item
   *  @param cardId Database column card_id SqlType(INT), PrimaryKey
   *  @param light Database column light SqlType(INT)
   *  @param cost Database column cost SqlType(INT)
   *  @param goldValue Database column gold_value SqlType(INT)
   *  @param victoryPoints Database column victory_points SqlType(INT) */
  case class ItemRow(cardId: Int, light: Int, cost: Int, goldValue: Int, victoryPoints: Int)
  /** GetResult implicit for fetching ItemRow objects using plain SQL queries */
  implicit def GetResultItemRow(implicit e0: GR[Int]): GR[ItemRow] = GR{
    prs => import prs._
    ItemRow.tupled((<<[Int], <<[Int], <<[Int], <<[Int], <<[Int]))
  }
  /** Table description of table Item. Objects of this class serve as prototypes for rows in queries. */
  //noinspection ScalaUnnecessaryParentheses,DuplicatedCode
  class Item(_tableTag: Tag) extends profile.api.Table[ItemRow](_tableTag, Some("TStone"), "Item") {
    def * = (cardId, light, cost, goldValue, victoryPoints) <> (ItemRow.tupled, ItemRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(cardId), Rep.Some(light), Rep.Some(cost), Rep.Some(goldValue), Rep.Some(victoryPoints))).shaped.<>({r=>import r._; _1.map(_=> ItemRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column card_id SqlType(INT), PrimaryKey */
    val cardId: Rep[Int] = column[Int]("card_id", O.PrimaryKey)
    /** Database column light SqlType(INT) */
    val light: Rep[Int] = column[Int]("light")
    /** Database column cost SqlType(INT) */
    val cost: Rep[Int] = column[Int]("cost")
    /** Database column gold_value SqlType(INT) */
    val goldValue: Rep[Int] = column[Int]("gold_value")
    /** Database column victory_points SqlType(INT) */
    val victoryPoints: Rep[Int] = column[Int]("victory_points")
  }
  /** Collection-like TableQuery object for table Item */
  lazy val Item = new TableQuery(tag => new Item(tag))
}
