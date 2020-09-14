package models.schema
// AUTO-GENERATED Slick data model for table UserConfirmation
trait UserConfirmationTable {

  self:Tables  =>

  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}
  /** Entity class storing rows of table UserConfirmation
   *  @param userConfirmationId Database column user_confirmation_id SqlType(INT), AutoInc, PrimaryKey
   *  @param token Database column token SqlType(VARCHAR), Length(256,true)
   *  @param userId Database column user_id SqlType(INT) */
  case class UserConfirmationRow(userConfirmationId: Int, token: String, userId: Int)
  /** GetResult implicit for fetching UserConfirmationRow objects using plain SQL queries */
  implicit def GetResultUserConfirmationRow(implicit e0: GR[Int], e1: GR[String]): GR[UserConfirmationRow] = GR{
    prs => import prs._
    UserConfirmationRow.tupled((<<[Int], <<[String], <<[Int]))
  }
  /** Table description of table UserConfirmation. Objects of this class serve as prototypes for rows in queries. */
  //noinspection ScalaUnnecessaryParentheses
  class UserConfirmation(_tableTag: Tag) extends profile.api.Table[UserConfirmationRow](_tableTag, Some("TStone"), "UserConfirmation") {
    def * = (userConfirmationId, token, userId) <> (UserConfirmationRow.tupled, UserConfirmationRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(userConfirmationId), Rep.Some(token), Rep.Some(userId))).shaped.<>({r=>import r._; _1.map(_=> UserConfirmationRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column user_confirmation_id SqlType(INT), AutoInc, PrimaryKey */
    val userConfirmationId: Rep[Int] = column[Int]("user_confirmation_id", O.AutoInc, O.PrimaryKey)
    /** Database column token SqlType(VARCHAR), Length(256,true) */
    val token: Rep[String] = column[String]("token", O.Length(256,varying=true))
    /** Database column user_id SqlType(INT) */
    val userId: Rep[Int] = column[Int]("user_id")

    /** Uniqueness Index over (token) (database name confirmation_token) */
    val index1 = index("confirmation_token", token, unique=true)
    /** Uniqueness Index over (userId) (database name confirmation_user) */
    val index2 = index("confirmation_user", userId, unique=true)
  }
  /** Collection-like TableQuery object for table UserConfirmation */
  lazy val UserConfirmation = new TableQuery(tag => new UserConfirmation(tag))
}
