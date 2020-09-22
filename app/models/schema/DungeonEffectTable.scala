package models.schema
// AUTO-GENERATED Slick data model for table DungeonEffect
trait DungeonEffectTable {

  self:Tables  =>

  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}
  /** Entity class storing rows of table DungeonEffect
   *  @param effectId Database column effect_id SqlType(INT), AutoInc, PrimaryKey
   *  @param cardId Database column card_id SqlType(INT)
   *  @param effect Database column effect SqlType(VARCHAR), Length(200,true), Default(None)
   *  @param needType Database column need_type SqlType(VARCHAR), Length(100,true), Default(None)
   *  @param repeatable Database column repeatable SqlType(BIT)
   *  @param operation Database column operation SqlType(ENUM), Length(9,false), Default(None)
   *  @param modifierAmount Database column modifier_amount SqlType(INT), Default(None)
   *  @param attributeModified Database column attribute_modified SqlType(ENUM), Length(10,false), Default(None) */
  case class DungeonEffectRow(effectId: Int, cardId: Int, effect: Option[String] = None, needType: Option[String] = None, repeatable: Boolean, operation: Option[String] = None, modifierAmount: Option[Int] = None, attributeModified: Option[String] = None)
  /** GetResult implicit for fetching DungeonEffectRow objects using plain SQL queries */
  implicit def GetResultDungeonEffectRow(implicit e0: GR[Int], e1: GR[Option[String]], e2: GR[Boolean], e3: GR[Option[Int]]): GR[DungeonEffectRow] = GR{
    prs => import prs._
    DungeonEffectRow.tupled((<<[Int], <<[Int], <<?[String], <<?[String], <<[Boolean], <<?[String], <<?[Int], <<?[String]))
  }
  /** Table description of table DungeonEffect. Objects of this class serve as prototypes for rows in queries. */
  class DungeonEffect(_tableTag: Tag) extends profile.api.Table[DungeonEffectRow](_tableTag, Some("TStone"), "DungeonEffect") {
    def * = (effectId, cardId, effect, needType, repeatable, operation, modifierAmount, attributeModified) <> (DungeonEffectRow.tupled, DungeonEffectRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(effectId), Rep.Some(cardId), effect, needType, Rep.Some(repeatable), operation, modifierAmount, attributeModified)).shaped.<>({r=>import r._; _1.map(_=> DungeonEffectRow.tupled((_1.get, _2.get, _3, _4, _5.get, _6, _7, _8)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column effect_id SqlType(INT), AutoInc, PrimaryKey */
    val effectId: Rep[Int] = column[Int]("effect_id", O.AutoInc, O.PrimaryKey)
    /** Database column card_id SqlType(INT) */
    val cardId: Rep[Int] = column[Int]("card_id")
    /** Database column effect SqlType(VARCHAR), Length(200,true), Default(None) */
    val effect: Rep[Option[String]] = column[Option[String]]("effect", O.Length(200,varying=true), O.Default(None))
    /** Database column need_type SqlType(VARCHAR), Length(100,true), Default(None) */
    val needType: Rep[Option[String]] = column[Option[String]]("need_type", O.Length(100,varying=true), O.Default(None))
    /** Database column repeatable SqlType(BIT) */
    val repeatable: Rep[Boolean] = column[Boolean]("repeatable")
    /** Database column operation SqlType(ENUM), Length(9,false), Default(None) */
    val operation: Rep[Option[String]] = column[Option[String]]("operation", O.Length(9,varying=false), O.Default(None))
    /** Database column modifier_amount SqlType(INT), Default(None) */
    val modifierAmount: Rep[Option[Int]] = column[Option[Int]]("modifier_amount", O.Default(None))
    /** Database column attribute_modified SqlType(ENUM), Length(10,false), Default(None) */
    val attributeModified: Rep[Option[String]] = column[Option[String]]("attribute_modified", O.Length(10,varying=false), O.Default(None))

    /** Index over (cardId) (database name card_id) */
    val index1 = index("card_id", cardId)
  }
  /** Collection-like TableQuery object for table DungeonEffect */
  lazy val DungeonEffect = new TableQuery(tag => new DungeonEffect(tag))
}
