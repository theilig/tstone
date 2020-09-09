package models.schema
// AUTO-GENERATED Slick data model for table VillageEffect
trait VillageEffectTable {

  self:Tables  =>

  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}
  /** Entity class storing rows of table VillageEffect
   *  @param effectId Database column effect_id SqlType(INT), AutoInc, PrimaryKey
   *  @param cardId Database column card_id SqlType(INT)
   *  @param effect Database column effect SqlType(VARCHAR), Length(200,true) */
  case class VillageEffectRow(effectId: Int, cardId: Int, effect: String)
  /** GetResult implicit for fetching VillageEffectRow objects using plain SQL queries */
  implicit def GetResultVillageEffectRow(implicit e0: GR[Int], e1: GR[String]): GR[VillageEffectRow] = GR{
    prs => import prs._
    VillageEffectRow.tupled((<<[Int], <<[Int], <<[String]))
  }
  /** Table description of table VillageEffect. Objects of this class serve as prototypes for rows in queries. */
  class VillageEffect(_tableTag: Tag) extends profile.api.Table[VillageEffectRow](_tableTag, Some("TSDev"), "VillageEffect") {
    def * = (effectId, cardId, effect) <> (VillageEffectRow.tupled, VillageEffectRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(effectId), Rep.Some(cardId), Rep.Some(effect))).shaped.<>({r=>import r._; _1.map(_=> VillageEffectRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column effect_id SqlType(INT), AutoInc, PrimaryKey */
    val effectId: Rep[Int] = column[Int]("effect_id", O.AutoInc, O.PrimaryKey)
    /** Database column card_id SqlType(INT) */
    val cardId: Rep[Int] = column[Int]("card_id")
    /** Database column effect SqlType(VARCHAR), Length(200,true) */
    val effect: Rep[String] = column[String]("effect", O.Length(200,varying=true))

    /** Index over (cardId) (database name card_id) */
    val index1 = index("card_id", cardId)
  }
  /** Collection-like TableQuery object for table VillageEffect */
  lazy val VillageEffect = new TableQuery(tag => new VillageEffect(tag))
}
