package models.schema
// AUTO-GENERATED Slick data model for table Tokens
trait TokensTable {

  self:Tables  =>

  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}
  /** Entity class storing rows of table Tokens
   *  @param tokenId Database column token_id SqlType(INT), AutoInc, PrimaryKey
   *  @param expires Database column expires SqlType(INT)
   *  @param userId Database column user_id SqlType(INT)
   *  @param tokenValue Database column token_value SqlType(VARCHAR), Length(256,true) */
  case class TokensRow(tokenId: Int, expires: Int, userId: Int, tokenValue: String)
  /** GetResult implicit for fetching TokensRow objects using plain SQL queries */
  implicit def GetResultTokensRow(implicit e0: GR[Int], e1: GR[String]): GR[TokensRow] = GR{
    prs => import prs._
    TokensRow.tupled((<<[Int], <<[Int], <<[Int], <<[String]))
  }
  /** Table description of table Tokens. Objects of this class serve as prototypes for rows in queries. */
  //noinspection ScalaUnnecessaryParentheses
  class Tokens(_tableTag: Tag) extends profile.api.Table[TokensRow](_tableTag, Some("TStone"), "Tokens") {
    def * = (tokenId, expires, userId, tokenValue) <> (TokensRow.tupled, TokensRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(tokenId), Rep.Some(expires), Rep.Some(userId), Rep.Some(tokenValue))).shaped.<>({r=>import r._; _1.map(_=> TokensRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column token_id SqlType(INT), AutoInc, PrimaryKey */
    val tokenId: Rep[Int] = column[Int]("token_id", O.AutoInc, O.PrimaryKey)
    /** Database column expires SqlType(INT) */
    val expires: Rep[Int] = column[Int]("expires")
    /** Database column user_id SqlType(INT) */
    val userId: Rep[Int] = column[Int]("user_id")
    /** Database column token_value SqlType(VARCHAR), Length(256,true) */
    val tokenValue: Rep[String] = column[String]("token_value", O.Length(256,varying=true))

    /** Uniqueness Index over (tokenValue) (database name token_value) */
    val index1 = index("token_value", tokenValue, unique=true)
    /** Uniqueness Index over (userId) (database name user_id) */
    val index2 = index("user_id", userId, unique=true)
  }
  /** Collection-like TableQuery object for table Tokens */
  lazy val Tokens = new TableQuery(tag => new Tokens(tag))
}
