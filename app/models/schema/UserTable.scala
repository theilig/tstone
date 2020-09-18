package models.schema
// AUTO-GENERATED Slick data model for table User
trait UserTable {

  self:Tables  =>

  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}
  /** Entity class storing rows of table User
   *  @param userId Database column user_id SqlType(INT), AutoInc, PrimaryKey
   *  @param firstName Database column first_name SqlType(VARCHAR), Length(200,true)
   *  @param lastName Database column last_name SqlType(VARCHAR), Length(200,true)
   *  @param passwordHash Database column password_hash SqlType(VARCHAR), Length(200,true)
   *  @param email Database column email SqlType(VARCHAR), Length(256,true)
   *  @param confirmed Database column confirmed SqlType(BIT), Default(false) */
  case class UserRow(userId: Int, firstName: String, lastName: String, passwordHash: String, email: String, confirmed: Boolean = false)
  /** GetResult implicit for fetching UserRow objects using plain SQL queries */
  implicit def GetResultUserRow(implicit e0: GR[Int], e1: GR[String], e2: GR[Boolean]): GR[UserRow] = GR{
    prs => import prs._
    UserRow.tupled((<<[Int], <<[String], <<[String], <<[String], <<[String], <<[Boolean]))
  }
  /** Table description of table User. Objects of this class serve as prototypes for rows in queries. */
  class User(_tableTag: Tag) extends profile.api.Table[UserRow](_tableTag, Some("TStone"), "User") {
    def * = (userId, firstName, lastName, passwordHash, email, confirmed) <> (UserRow.tupled, UserRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(userId), Rep.Some(firstName), Rep.Some(lastName), Rep.Some(passwordHash), Rep.Some(email), Rep.Some(confirmed))).shaped.<>({r=>import r._; _1.map(_=> UserRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column user_id SqlType(INT), AutoInc, PrimaryKey */
    val userId: Rep[Int] = column[Int]("user_id", O.AutoInc, O.PrimaryKey)
    /** Database column first_name SqlType(VARCHAR), Length(200,true) */
    val firstName: Rep[String] = column[String]("first_name", O.Length(200,varying=true))
    /** Database column last_name SqlType(VARCHAR), Length(200,true) */
    val lastName: Rep[String] = column[String]("last_name", O.Length(200,varying=true))
    /** Database column password_hash SqlType(VARCHAR), Length(200,true) */
    val passwordHash: Rep[String] = column[String]("password_hash", O.Length(200,varying=true))
    /** Database column email SqlType(VARCHAR), Length(256,true) */
    val email: Rep[String] = column[String]("email", O.Length(256,varying=true))
    /** Database column confirmed SqlType(BIT), Default(false) */
    val confirmed: Rep[Boolean] = column[Boolean]("confirmed", O.Default(false))

    /** Uniqueness Index over (email) (database name user_email) */
    val index1 = index("user_email", email, unique=true)
  }
  /** Collection-like TableQuery object for table User */
  lazy val User = new TableQuery(tag => new User(tag))
}
