package models.schema
// AUTO-GENERATED Slick data model for table Card
trait CardTable {

  self:Tables  =>

  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}
  /** Entity class storing rows of table Card
   *  @param cardId Database column card_id SqlType(INT), AutoInc, PrimaryKey
   *  @param image Database column image SqlType(VARCHAR), Length(200,true)
   *  @param name Database column name SqlType(VARCHAR), Length(100,true) */
  case class CardRow(cardId: Int, image: String, name: String)
  /** GetResult implicit for fetching CardRow objects using plain SQL queries */
  implicit def GetResultCardRow(implicit e0: GR[Int], e1: GR[String]): GR[CardRow] = GR{
    prs => import prs._
    CardRow.tupled((<<[Int], <<[String], <<[String]))
  }
  /** Table description of table Card. Objects of this class serve as prototypes for rows in queries. */
  class Card(_tableTag: Tag) extends profile.api.Table[CardRow](_tableTag, Some("TStone"), "Card") {
    def * = (cardId, image, name) <> (CardRow.tupled, CardRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(cardId), Rep.Some(image), Rep.Some(name))).shaped.<>({r=>import r._; _1.map(_=> CardRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column card_id SqlType(INT), AutoInc, PrimaryKey */
    val cardId: Rep[Int] = column[Int]("card_id", O.AutoInc, O.PrimaryKey)
    /** Database column image SqlType(VARCHAR), Length(200,true) */
    val image: Rep[String] = column[String]("image", O.Length(200,varying=true))
    /** Database column name SqlType(VARCHAR), Length(100,true) */
    val name: Rep[String] = column[String]("name", O.Length(100,varying=true))
  }
  /** Collection-like TableQuery object for table Card */
  lazy val Card = new TableQuery(tag => new Card(tag))
}
