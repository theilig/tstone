package dao

import javax.inject.Inject
import models.schema
import models.schema.Tables
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class CardDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
                        (implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  private val cards = TableQuery[Tables.Card]

  private val heros = TableQuery[Tables.Hero]

  private val spells = TableQuery[Tables.Spell]

  private val items = TableQuery[Tables.Item]

  private val villiagers = TableQuery[Tables.Villager]

  private val weapons = TableQuery[Tables.Weapon]

  def findByNames(names: List[String]): Future[Seq[Tables.CardRow]] = {
    db.run(cards.filter(_.name.inSet(names)).result)
  }

  def getHeros: Future[List[schema.Tables.HeroRow]] = {
    val query = for {
      (_, hero) <- cards join heros on (_.cardId === _.cardId)
    } yield hero
    db.run(query.result).map(s => s.toList)
  }
  def getWeapons: Future[List[schema.Tables.WeaponRow]] = {
    val query = for {
      (_, weapon) <- cards join weapons on (_.cardId === _.cardId)
    } yield weapon
    db.run(query.result).map(s => s.toList)
  }
  def getSpells: Future[List[schema.Tables.SpellRow]] = {
    val query = for {
      (_, spell) <- cards join spells on (_.cardId === _.cardId)
    } yield spell
    db.run(query.result).map(s => s.toList)
  }
  def getItems: Future[List[schema.Tables.ItemRow]] = {
    val query = for {
      (_, item) <- cards join items on (_.cardId === _.cardId)
    } yield item
    db.run(query.result).map(s => s.toList)
  }
  def getVilliagers: Future[List[schema.Tables.VillagerRow]] = {
    val query = for {
      (_, villager) <- cards join villiagers on (_.cardId === _.cardId)
    } yield villager
    db.run(query.result).map(s => s.toList)
  }
}
