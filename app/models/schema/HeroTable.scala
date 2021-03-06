package models.schema
// AUTO-GENERATED Slick data model for table Hero
trait HeroTable {

  self:Tables  =>

  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}
  /** Entity class storing rows of table Hero
   *  @param cardId Database column card_id SqlType(INT), PrimaryKey
   *  @param light Database column light SqlType(INT)
   *  @param strength Database column strength SqlType(INT)
   *  @param level Database column level SqlType(INT)
   *  @param cost Database column cost SqlType(INT)
   *  @param goldValue Database column gold_value SqlType(INT), Default(None)
   *  @param victoryPoints Database column victory_points SqlType(INT)
   *  @param heroType Database column hero_type SqlType(VARCHAR), Length(50,true)
   *  @param heroClasses Database column hero_classes SqlType(VARCHAR), Length(100,true) */
  case class HeroRow(cardId: Int, light: Int, strength: Int, level: Int, cost: Int, goldValue: Option[Int] = None, victoryPoints: Int, heroType: String, heroClasses: String)
  /** GetResult implicit for fetching HeroRow objects using plain SQL queries */
  implicit def GetResultHeroRow(implicit e0: GR[Int], e1: GR[Option[Int]], e2: GR[String]): GR[HeroRow] = GR{
    prs => import prs._
    HeroRow.tupled((<<[Int], <<[Int], <<[Int], <<[Int], <<[Int], <<?[Int], <<[Int], <<[String], <<[String]))
  }
  /** Table description of table Hero. Objects of this class serve as prototypes for rows in queries. */
  class Hero(_tableTag: Tag) extends profile.api.Table[HeroRow](_tableTag, Some("TStone"), "Hero") {
    def * = (cardId, light, strength, level, cost, goldValue, victoryPoints, heroType, heroClasses) <> (HeroRow.tupled, HeroRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(cardId), Rep.Some(light), Rep.Some(strength), Rep.Some(level), Rep.Some(cost), goldValue, Rep.Some(victoryPoints), Rep.Some(heroType), Rep.Some(heroClasses))).shaped.<>({r=>import r._; _1.map(_=> HeroRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6, _7.get, _8.get, _9.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column card_id SqlType(INT), PrimaryKey */
    val cardId: Rep[Int] = column[Int]("card_id", O.PrimaryKey)
    /** Database column light SqlType(INT) */
    val light: Rep[Int] = column[Int]("light")
    /** Database column strength SqlType(INT) */
    val strength: Rep[Int] = column[Int]("strength")
    /** Database column level SqlType(INT) */
    val level: Rep[Int] = column[Int]("level")
    /** Database column cost SqlType(INT) */
    val cost: Rep[Int] = column[Int]("cost")
    /** Database column gold_value SqlType(INT), Default(None) */
    val goldValue: Rep[Option[Int]] = column[Option[Int]]("gold_value", O.Default(None))
    /** Database column victory_points SqlType(INT) */
    val victoryPoints: Rep[Int] = column[Int]("victory_points")
    /** Database column hero_type SqlType(VARCHAR), Length(50,true) */
    val heroType: Rep[String] = column[String]("hero_type", O.Length(50,varying=true))
    /** Database column hero_classes SqlType(VARCHAR), Length(100,true) */
    val heroClasses: Rep[String] = column[String]("hero_classes", O.Length(100,varying=true))

    /** Index over (heroType) (database name hero_type) */
    val index1 = index("hero_type", heroType)
  }
  /** Collection-like TableQuery object for table Hero */
  lazy val Hero = new TableQuery(tag => new Hero(tag))
}
