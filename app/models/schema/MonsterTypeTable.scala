package models.schema
// AUTO-GENERATED Slick data model for table MonsterType
trait MonsterTypeTable {

  self:Tables  =>

  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}
  /** Entity class storing rows of table MonsterType
   *  @param traitId Database column trait_id SqlType(INT), AutoInc, PrimaryKey
   *  @param cardId Database column card_id SqlType(INT)
   *  @param `trait` Database column trait SqlType(VARCHAR), Length(100,true) */
  case class MonsterTypeRow(traitId: Int, cardId: Int, `trait`: String)
  /** GetResult implicit for fetching MonsterTypeRow objects using plain SQL queries */
  implicit def GetResultMonsterTypeRow(implicit e0: GR[Int], e1: GR[String]): GR[MonsterTypeRow] = GR{
    prs => import prs._
    MonsterTypeRow.tupled((<<[Int], <<[Int], <<[String]))
  }
  /** Table description of table MonsterType. Objects of this class serve as prototypes for rows in queries.
   *  NOTE: The following names collided with Scala keywords and were escaped: trait */
  //noinspection ScalaUnnecessaryParentheses
  class MonsterType(_tableTag: Tag) extends profile.api.Table[MonsterTypeRow](_tableTag, Some("TStone"), "MonsterType") {
    def * = (traitId, cardId, `trait`) <> (MonsterTypeRow.tupled, MonsterTypeRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(traitId), Rep.Some(cardId), Rep.Some(`trait`))).shaped.<>({r=>import r._; _1.map(_=> MonsterTypeRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column trait_id SqlType(INT), AutoInc, PrimaryKey */
    val traitId: Rep[Int] = column[Int]("trait_id", O.AutoInc, O.PrimaryKey)
    /** Database column card_id SqlType(INT) */
    val cardId: Rep[Int] = column[Int]("card_id")
    /** Database column trait SqlType(VARCHAR), Length(100,true)
     *  NOTE: The name was escaped because it collided with a Scala keyword. */
    val `trait`: Rep[String] = column[String]("trait", O.Length(100,varying=true))

    /** Index over (cardId) (database name card_id) */
    val index1 = index("card_id", cardId)
  }
  /** Collection-like TableQuery object for table MonsterType */
  lazy val MonsterType = new TableQuery(tag => new MonsterType(tag))
}
