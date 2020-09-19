package models.schema
// AUTO-GENERATED Slick data model for table Monster
trait MonsterTable {

  self:Tables  =>

  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}
  /** Entity class storing rows of table Monster
   *  @param cardId Database column card_id SqlType(INT), PrimaryKey
   *  @param light Database column light SqlType(INT)
   *  @param health Database column health SqlType(INT)
   *  @param goldValue Database column gold_value SqlType(INT)
   *  @param victoryPoints Database column victory_points SqlType(INT)
   *  @param frequency Database column frequency SqlType(INT)
   *  @param experience Database column experience SqlType(INT) */
  case class MonsterRow(cardId: Int, light: Int, health: Int, goldValue: Int, victoryPoints: Int, frequency: Int, experience: Int)
  /** GetResult implicit for fetching MonsterRow objects using plain SQL queries */
  implicit def GetResultMonsterRow(implicit e0: GR[Int]): GR[MonsterRow] = GR{
    prs => import prs._
    MonsterRow.tupled((<<[Int], <<[Int], <<[Int], <<[Int], <<[Int], <<[Int], <<[Int]))
  }
  /** Table description of table Monster. Objects of this class serve as prototypes for rows in queries. */
  class Monster(_tableTag: Tag) extends profile.api.Table[MonsterRow](_tableTag, Some("TStone"), "Monster") {
    def * = (cardId, light, health, goldValue, victoryPoints, frequency, experience) <> (MonsterRow.tupled, MonsterRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(cardId), Rep.Some(light), Rep.Some(health), Rep.Some(goldValue), Rep.Some(victoryPoints), Rep.Some(frequency), Rep.Some(experience))).shaped.<>({r=>import r._; _1.map(_=> MonsterRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column card_id SqlType(INT), PrimaryKey */
    val cardId: Rep[Int] = column[Int]("card_id", O.PrimaryKey)
    /** Database column light SqlType(INT) */
    val light: Rep[Int] = column[Int]("light")
    /** Database column health SqlType(INT) */
    val health: Rep[Int] = column[Int]("health")
    /** Database column gold_value SqlType(INT) */
    val goldValue: Rep[Int] = column[Int]("gold_value")
    /** Database column victory_points SqlType(INT) */
    val victoryPoints: Rep[Int] = column[Int]("victory_points")
    /** Database column frequency SqlType(INT) */
    val frequency: Rep[Int] = column[Int]("frequency")
    /** Database column experience SqlType(INT) */
    val experience: Rep[Int] = column[Int]("experience")
  }
  /** Collection-like TableQuery object for table Monster */
  lazy val Monster = new TableQuery(tag => new Monster(tag))
}
