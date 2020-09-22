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
   *  @param victoryPoints Database column victory_points SqlType(INT)
   *  @param weaponTypes Database column weapon_types SqlType(VARCHAR), Length(100,true) */
  case class WeaponRow(cardId: Int, light: Int, weight: Int, cost: Int, goldValue: Int, victoryPoints: Int, weaponTypes: String)
  /** GetResult implicit for fetching WeaponRow objects using plain SQL queries */
  implicit def GetResultWeaponRow(implicit e0: GR[Int], e1: GR[String]): GR[WeaponRow] = GR{
    prs => import prs._
    WeaponRow.tupled((<<[Int], <<[Int], <<[Int], <<[Int], <<[Int], <<[Int], <<[String]))
  }
  /** Table description of table Weapon. Objects of this class serve as prototypes for rows in queries. */
  class Weapon(_tableTag: Tag) extends profile.api.Table[WeaponRow](_tableTag, Some("TStone"), "Weapon") {
    def * = (cardId, light, weight, cost, goldValue, victoryPoints, weaponTypes) <> (WeaponRow.tupled, WeaponRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(cardId), Rep.Some(light), Rep.Some(weight), Rep.Some(cost), Rep.Some(goldValue), Rep.Some(victoryPoints), Rep.Some(weaponTypes))).shaped.<>({r=>import r._; _1.map(_=> WeaponRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

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
    /** Database column weapon_types SqlType(VARCHAR), Length(100,true) */
    val weaponTypes: Rep[String] = column[String]("weapon_types", O.Length(100,varying=true))
  }
  /** Collection-like TableQuery object for table Weapon */
  lazy val Weapon = new TableQuery(tag => new Weapon(tag))
}
