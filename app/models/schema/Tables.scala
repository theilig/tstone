package models.schema
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = slick.jdbc.MySQLProfile
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.)
    Each generated XXXXTable trait is mixed in this trait hence allowing access to all the TableQuery lazy vals.
  */
trait Tables extends CardTable with ItemTable with BreachEffectTable with UserConfirmationTable with PlayEvolutionsTable with ThunderstoneTable with GameTable with MonsterTable with TokensTable with SpellTable with TurnEffectTable with VillagerTable with HeroTable with UserTable with WeaponTable {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = Array(BreachEffect.schema, Card.schema, Game.schema, Hero.schema, Item.schema, Monster.schema, PlayEvolutions.schema, Spell.schema, Thunderstone.schema, Tokens.schema, TurnEffect.schema, User.schema, UserConfirmation.schema, Villager.schema, Weapon.schema).reduceLeft(_ ++ _)
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

}
