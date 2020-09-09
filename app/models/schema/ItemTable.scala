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
   *  @param name Database column name SqlType(VARCHAR), Length(100,true)
   *  @param light Database column light SqlType(INT)
   *  @param weight Database column weight SqlType(INT), Default(None)
   *  @param cost Database column cost SqlType(INT)
   *  @param goldValue Database column gold_value SqlType(INT)
   *  @param victoryPoints Database column victory_points SqlType(INT) */
  case class ItemRow(cardId: Int, name: String, light: Int, weight: Option[Int] = None, cost: Int, goldValue: Int, victoryPoints: Int)
  /** GetResult implicit for fetching ItemRow objects using plain SQL queries */
  implicit def GetResultItemRow(implicit e0: GR[Int], e1: GR[String], e2: GR[Option[Int]]): GR[ItemRow] = GR{
    prs => import prs._
    ItemRow.tupled((<<[Int], <<[String], <<[Int], <<?[Int], <<[Int], <<[Int], <<[Int]))
  }
  /** Table description of table Item. Objects of this class serve as prototypes for rows in queries. */
  class Item(_tableTag: Tag) extends profile.api.Table[ItemRow](_tableTag, Some("TSDev"), "Item") {
    def * = (cardId, name, light, weight, cost, goldValue, victoryPoints) <> (ItemRow.tupled, ItemRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(cardId), Rep.Some(name), Rep.Some(light), weight, Rep.Some(cost), Rep.Some(goldValue), Rep.Some(victoryPoints))).shaped.<>({r=>import r._; _1.map(_=> ItemRow.tupled((_1.get, _2.get, _3.get, _4, _5.get, _6.get, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column card_id SqlType(INT), PrimaryKey */
    val cardId: Rep[Int] = column[Int]("card_id", O.PrimaryKey)
    /** Database column name SqlType(VARCHAR), Length(100,true) */
    val name: Rep[String] = column[String]("name", O.Length(100,varying=true))
    /** Database column light SqlType(INT) */
    val light: Rep[Int] = column[Int]("light")
    /** Database column weight SqlType(INT), Default(None) */
    val weight: Rep[Option[Int]] = column[Option[Int]]("weight", O.Default(None))
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
