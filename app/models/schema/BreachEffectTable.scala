package models.schema
// AUTO-GENERATED Slick data model for table BreachEffect
trait BreachEffectTable {

  self:Tables  =>

  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}
  /** Entity class storing rows of table BreachEffect
   *  @param effectId Database column effect_id SqlType(INT), AutoInc, PrimaryKey
   *  @param cardId Database column card_id SqlType(INT)
   *  @param effect Database column effect SqlType(VARCHAR), Length(200,true) */
  case class BreachEffectRow(effectId: Int, cardId: Int, effect: String)
  /** GetResult implicit for fetching BreachEffectRow objects using plain SQL queries */
  implicit def GetResultBreachEffectRow(implicit e0: GR[Int], e1: GR[String]): GR[BreachEffectRow] = GR{
    prs => import prs._
    BreachEffectRow.tupled((<<[Int], <<[Int], <<[String]))
  }
  /** Table description of table BreachEffect. Objects of this class serve as prototypes for rows in queries. */
  class BreachEffect(_tableTag: Tag) extends profile.api.Table[BreachEffectRow](_tableTag, Some("TSDev"), "BreachEffect") {
    def * = (effectId, cardId, effect) <> (BreachEffectRow.tupled, BreachEffectRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(effectId), Rep.Some(cardId), Rep.Some(effect))).shaped.<>({r=>import r._; _1.map(_=> BreachEffectRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column effect_id SqlType(INT), AutoInc, PrimaryKey */
    val effectId: Rep[Int] = column[Int]("effect_id", O.AutoInc, O.PrimaryKey)
    /** Database column card_id SqlType(INT) */
    val cardId: Rep[Int] = column[Int]("card_id")
    /** Database column effect SqlType(VARCHAR), Length(200,true) */
    val effect: Rep[String] = column[String]("effect", O.Length(200,varying=true))

    /** Index over (cardId) (database name card_id) */
    val index1 = index("card_id", cardId)
  }
  /** Collection-like TableQuery object for table BreachEffect */
  lazy val BreachEffect = new TableQuery(tag => new BreachEffect(tag))
}
