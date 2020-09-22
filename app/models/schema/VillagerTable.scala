package models.schema
// AUTO-GENERATED Slick data model for table Villager
trait VillagerTable {

  self:Tables  =>

  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}
  /** Entity class storing rows of table Villager
   *  @param cardId Database column card_id SqlType(INT), PrimaryKey
   *  @param cost Database column cost SqlType(INT)
   *  @param goldValue Database column gold_value SqlType(INT), Default(None)
   *  @param victoryPoints Database column victory_points SqlType(INT)
   *  @param villagerTypes Database column villager_types SqlType(VARCHAR), Length(100,true) */
  case class VillagerRow(cardId: Int, cost: Int, goldValue: Option[Int] = None, victoryPoints: Int, villagerTypes: String)
  /** GetResult implicit for fetching VillagerRow objects using plain SQL queries */
  implicit def GetResultVillagerRow(implicit e0: GR[Int], e1: GR[Option[Int]], e2: GR[String]): GR[VillagerRow] = GR{
    prs => import prs._
    VillagerRow.tupled((<<[Int], <<[Int], <<?[Int], <<[Int], <<[String]))
  }
  /** Table description of table Villager. Objects of this class serve as prototypes for rows in queries. */
  class Villager(_tableTag: Tag) extends profile.api.Table[VillagerRow](_tableTag, Some("TStone"), "Villager") {
    def * = (cardId, cost, goldValue, victoryPoints, villagerTypes) <> (VillagerRow.tupled, VillagerRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(cardId), Rep.Some(cost), goldValue, Rep.Some(victoryPoints), Rep.Some(villagerTypes))).shaped.<>({r=>import r._; _1.map(_=> VillagerRow.tupled((_1.get, _2.get, _3, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column card_id SqlType(INT), PrimaryKey */
    val cardId: Rep[Int] = column[Int]("card_id", O.PrimaryKey)
    /** Database column cost SqlType(INT) */
    val cost: Rep[Int] = column[Int]("cost")
    /** Database column gold_value SqlType(INT), Default(None) */
    val goldValue: Rep[Option[Int]] = column[Option[Int]]("gold_value", O.Default(None))
    /** Database column victory_points SqlType(INT) */
    val victoryPoints: Rep[Int] = column[Int]("victory_points")
    /** Database column villager_types SqlType(VARCHAR), Length(100,true) */
    val villagerTypes: Rep[String] = column[String]("villager_types", O.Length(100,varying=true))
  }
  /** Collection-like TableQuery object for table Villager */
  lazy val Villager = new TableQuery(tag => new Villager(tag))
}
