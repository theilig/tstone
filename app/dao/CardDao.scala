package dao

import javax.inject.Inject
import models.game.{Card, MonsterCard}
import models.{CardInfo, schema}
import models.schema.Tables
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class CardDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
                        (implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  private val cards = TableQuery[Tables.Card]
  private val heroes = TableQuery[Tables.Hero]
  private val spells = TableQuery[Tables.Spell]
  private val items = TableQuery[Tables.Item]
  private val villagers = TableQuery[Tables.Villager]
  private val weapons = TableQuery[Tables.Weapon]
  private val monsters = TableQuery[Tables.Monster]
  private val breachEffects = TableQuery[Tables.BreachEffect]
  private val turnEffects = TableQuery[Tables.TurnEffect]
  private val thunderstoneQuery = TableQuery[Tables.Thunderstone]


  private val heroQuery = for {
    (card, hero) <- cards join heroes on (_.cardId === _.cardId)
  } yield (card, hero)


  private val spellQuery = for {
    (card, spell) <- cards join spells on (_.cardId === _.cardId)
  } yield (card, spell)


  private val itemQuery = for {
    (card, item) <- cards join items on (_.cardId === _.cardId)
  } yield (card, item)


  private val villagerQuery = for {
    (card, villager) <- cards join villagers on (_.cardId === _.cardId)
  } yield (card, villager)


  private val weaponQuery = for {
    (card, weapon) <- cards join weapons on (_.cardId === _.cardId)
  } yield (card, weapon)

  def findByNames(names: List[String]): Future[Seq[Tables.CardRow]] = {
    db.run(cards.filter(_.name.inSet(names)).result)
  }

  def getMonstersByType: Future[Map[String, Seq[MonsterCard]]] = {
    val monsterQuery = for {
      (card, monster) <- cards join monsters on (_.cardId === _.cardId)
    } yield (card, monster)
    val eventualMonsterPairs = db.run(monsterQuery.result)
    val eventualBreachEffectRows = eventualMonsterPairs.flatMap(monsterPairs => {
      val monsterCardIds = monsterPairs.map(pair => pair._1.cardId)
      db.run(breachEffects.filter(_.cardId.inSet(monsterCardIds)).result)
    })
    val eventualTurnEffectRows = eventualMonsterPairs.flatMap(monsterPairs => {
      val monsterCardIds = monsterPairs.map(pair => pair._1.cardId)
      db.run(turnEffects.filter(_.cardId.inSet(monsterCardIds)).result)
    })
    for {
      monsterPairs <- eventualMonsterPairs
      breachEffectRows <- eventualBreachEffectRows
      turnEffectRows <- eventualTurnEffectRows
    } yield monsterPairs.map {
      case (card, monster) => MonsterCard(
        card,
        monster,
        turnEffectRows.filter(_.cardId == card.cardId),
        breachEffectRows.filter(_.cardId == card.cardId)
      )
    }.groupBy(m => m.monsterType)
  }

  private def getCardInfoByIds(cardIds: List[Int]): Future[Map[Int, CardInfo]] = {
    val higherLevelHeroIds = for {
      l1 <- heroes
      h <- heroes if l1.cardId.inSet(cardIds) && l1.heroType === h.heroType && h.level > 1
    } yield h.cardId
    db.run(higherLevelHeroIds.result).flatMap(extraIds => {
      val allCardIds = cardIds ::: extraIds.toList
      val cardRows = db.run(cards.filter(_.cardId.inSet(allCardIds)).result)
      val heroRows = db.run(heroes.filter(_.cardId.inSet(allCardIds)).result)
      val itemRows = db.run(items.filter(_.cardId.inSet(allCardIds)).result)
      val spellRows = db.run(spells.filter(_.cardId.inSet(allCardIds)).result)
      val villagerRows = db.run(villagers.filter(_.cardId.inSet(allCardIds)).result)
      val weaponsRows = db.run(weapons.filter(_.cardId.inSet(allCardIds)).result)
      val turnEffectRows = db.run(turnEffects.filter(_.cardId.inSet(allCardIds)).result)
      val breachEffectRows = db.run(breachEffects.filter(_.cardId.inSet(allCardIds)).result)
      val monsterRows = db.run(monsters.filter(_.cardId.inSet(allCardIds)).result)
      val thunderstoneRows = db.run(thunderstoneQuery.filter(_.cardId.inSet(allCardIds)).result)
      for {
        cards <- cardRows
        heroes <- heroRows
        items <- itemRows
        spells <- spellRows
        villagers <- villagerRows
        weapons <- weaponsRows
        turnEffects <- turnEffectRows
        breachEffects <- breachEffectRows
        monsters <- monsterRows
        thunderstones <- thunderstoneRows
      } yield {
        allCardIds.map(cardId => {
          cardId -> CardInfo(
            cards.find(_.cardId == cardId).get,
            heroes.find(_.cardId == cardId),
            items.find(_.cardId == cardId),
            monsters.find(_.cardId == cardId),
            spells.find(_.cardId == cardId),
            villagers.find(_.cardId == cardId),
            weapons.find(_.cardId == cardId),
            turnEffects.filter(_.cardId == cardId),
            breachEffects.filter(_.cardId == cardId),
            thunderstones.filter(_.cardId == cardId)
          )
        }).toMap
      }
    })
  }

  def findByIds(cardIds: List[Int]): Future[Map[Int,Card]] = {
    getCardInfoByIds(cardIds).map(cardInfoMap => {
      cardInfoMap.map {
        case (index, info) => index -> Card(info)
      }
    })
  }

  def getHeros: Future[List[schema.Tables.HeroRow]] = {
    db.run(heroQuery.result).map(s => s.flatMap({
      case (_, hero) if hero.level == 1 => Some(hero)
      case _ => None
    }).toList)
  }
  def getWeapons: Future[List[schema.Tables.WeaponRow]] = {
    db.run(weaponQuery.result).map(s => s.map(_._2).toList)
  }
  def getSpells: Future[List[schema.Tables.SpellRow]] = {
    db.run(spellQuery.result).map(s => s.map(_._2).toList)
  }
  def getItems: Future[List[schema.Tables.ItemRow]] = {
    db.run(itemQuery.result).map(s => s.map(_._2).toList)
  }
  def getVilliagers: Future[List[schema.Tables.VillagerRow]] = {
    db.run(villagerQuery.result).map(s => s.map(_._2).toList)
  }
}
