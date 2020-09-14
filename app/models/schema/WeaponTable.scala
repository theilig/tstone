package models.schema
// AUTO-GENERATED Slick data model for table Weapon
trait WeaponTable {

  self:Tables  =>

  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}
  /** Entity class storing rows of table Weapon
   *  @param cardId Database column card_id SqlType(INT), PrimaryKey
   *  @param light Database column light SqlType(INT)
   *  @param weight Database column weight SqlType(INT)
   *  @param cost Database column cost SqlType(INT)
   *  @param goldValue Database column gold_value SqlType(INT)
   *  @param victoryPoints Database column victory_points SqlType(INT) */
  case class WeaponRow(cardId: Int, light: Int, weight: Int, cost: Int, goldValue: Int, victoryPoints: Int)
  /** GetResult implicit for fetching WeaponRow objects using plain SQL queries */
  implicit def GetResultWeaponRow(implicit e0: GR[Int]): GR[WeaponRow] = GR{
    prs => import prs._
    WeaponRow.tupled((<<[Int], <<[Int], <<[Int], <<[Int], <<[Int], <<[Int]))
  }
  /** Table description of table Weapon. Objects of this class serve as prototypes for rows in queries. */
  //noinspection ScalaUnnecessaryParentheses
  class Weapon(_tableTag: Tag) extends profile.api.Table[WeaponRow](_tableTag, Some("TStone"), "Weapon") {
    def * = (cardId, light, weight, cost, goldValue, victoryPoints) <> (WeaponRow.tupled, WeaponRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(cardId), Rep.Some(light), Rep.Some(weight), Rep.Some(cost), Rep.Some(goldValue), Rep.Some(victoryPoints))).shaped.<>({r=>import r._; _1.map(_=> WeaponRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column card_id SqlType(INT), PrimaryKey */
    val cardId: Rep[Int] = column[Int]("card_id", O.PrimaryKey)
    /** Database column light SqlType(INT) */
    val light: Rep[Int] = column[Int]("light")
    /** Database column weight SqlType(INT) */
    val weight: Rep[Int] = column[Int]("weight")
    /** Database column cost SqlType(INT) */
    val cost: Rep[Int] = column[Int]("cost")
    /** Database column gold_value SqlType(INT) */
    val goldValue: Rep[Int] = column[Int]("gold_value")
    /** Database column victory_points SqlType(INT) */
    val victoryPoints: Rep[Int] = column[Int]("victory_points")
  }
  /** Collection-like TableQuery object for table Weapon */
  lazy val Weapon = new TableQuery(tag => new Weapon(tag))
}
