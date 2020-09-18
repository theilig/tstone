package models.game

import dao.CardDao
import models.schema.Tables.{HeroRow, ItemRow, WeaponRow, VillagerRow, SpellRow}
import play.api.libs.json.{Json, OFormat}

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class Village(
               heroes: List[HeroPile],
               weapons: List[WeaponPile],
               items: List[ItemPile],
               spells: List[SpellPile],
               villagers: List[VillagerPile]
             ) {
  def findPile(name: String): Pile = {
    val allPiles: List[Pile] = heroes ::: weapons ::: items ::: spells ::: villagers
    allPiles.find(p => p.cards.exists(c => c.getName == name)).get
  }
}

object Village {
  implicit val villageFormat: OFormat[Village] = Json.format[Village]
  def apply(startingCardIds: Seq[Int], cardIds: Seq[Int], cardDao: CardDao)
           (implicit ec: ExecutionContext): Future[Village] = {
    def isStartingCard(id: Int) = startingCardIds.contains(id)
    val eventualCards: Future[Map[Int, Card]] = cardDao.findByIds((startingCardIds ++ cardIds).toList)
    eventualCards.map(cards => {
      val heroesByType = cards.collect({
        case (_, h: HeroCard) => h
      }).groupBy(h => h.heroType)

      val piles = cards.map {
        case (id, h: HeroCard) => id -> HeroPile(heroesByType(h.heroType), isStartingCard(h.id))
        case (id, i: ItemCard) => id -> ItemPile(i, isStartingCard(i.id))
        case (id, s: SpellCard) => id -> SpellPile(s)
        case (id, v: VillagerCard) => id -> VillagerPile(v)
        case (id, w: WeaponCard) => id -> WeaponPile(w, isStartingCard(w.id))
      }
      new Village(
        piles.collect{case (_, h: HeroPile) => h}.toList,
        piles.collect{case (_, w: WeaponPile) => w}.toList,
        piles.collect{case (_, i: ItemPile) => i}.toList,
        piles.collect{case (_, s: SpellPile) => s}.toList,
        piles.collect{case (_, v: VillagerPile) => v}.toList
      )
    })
  }

  private def isVilliage(s: String) = {
    Set("Villager", "Item", "Spell", "Weapon").contains(s)
  }

  private def isHero(s: String) = s == "Hero"

  def pickVilliageCards(
                         randomIds: List[Int],
                         cardTypes: Map[Int, String],
                         remainingSlots: Map[String, Int],
                         heroCards: Int,
                         villageCards: Int
                       ): List[Int] = {
    @tailrec
    def pickVillageInternal(
                             randomIds: List[Int],
                             remainingSlots: Map[String, Int],
                             heroCards: Int,
                             villageCards: Int,
                             idsSoFar: List[Int]
                           ): List[Int] = {

      (villageCards, heroCards, cardTypes(randomIds.head)) match {
        case (0, 0, _) => idsSoFar
        case (x, _, t) if x > 0 && isVilliage(t) && remainingSlots(t) > 0 =>
          pickVillageInternal(
            randomIds.tail,
            remainingSlots + (t -> (remainingSlots(t) - 1)),
            heroCards,
            villageCards - 1,
            randomIds.head :: idsSoFar
          )
        case (_, x, t) if x > 0 && isHero(t) && remainingSlots(t) > 0 =>
          pickVillageInternal(
            randomIds.tail,
            remainingSlots + (t -> (remainingSlots(t) - 1)),
            heroCards - 1,
            villageCards,
            randomIds.head :: idsSoFar
          )
        case _ => pickVillageInternal(
          randomIds.tail,
          remainingSlots,
          heroCards,
          villageCards,
          idsSoFar
        )
      }
    }

    pickVillageInternal(randomIds, remainingSlots, heroCards, villageCards, Nil)
  }

  def drawRandomCardIds(
                         heroes: Map[Int, HeroRow],
                         spells: Map[Int, SpellRow],
                         weapons: Map[Int, WeaponRow],
                         items: Map[Int, ItemRow],
                         villagers: Map[Int, VillagerRow]
                       ): List[Int] = {
    val random = new Random
    val randomIds = random.shuffle((heroes.keys ++ spells.keys ++ weapons.keys ++ items.keys ++ villagers.keys).toList)
    @tailrec
    def randomIdsInternal(allIds: List[Int], cardLimits: Map[String, Int], ids: List[Int]): List[Int] = {
      val cardType = allIds.head match {
        case x if heroes.contains(x) => "Hero"
        case x if spells.contains(x) => "Spell"
        case x if weapons.contains(x) => "Weapon"
        case x if items.contains(x) => "Item"
        case x if villagers.contains(x) => "Villager"
      }
      cardType match {
        case _ if cardLimits("Hero") + cardLimits("Village") == 0 => ids
        case t if isHero(t) && cardLimits(t) > 0 =>
          randomIdsInternal(allIds.tail, cardLimits + (t -> (cardLimits(t) - 1)), allIds.head :: ids)
        case t if isVilliage(t) && cardLimits(t) > 0 && cardLimits("Village") > 0 =>
          randomIdsInternal(
            allIds.tail,
            cardLimits + (t -> (cardLimits(t) - 1)) + ("Village" -> (cardLimits("Village") - 1)),
            allIds.head :: ids
          )
        case _ => randomIdsInternal(allIds.tail, cardLimits, ids)
      }
    }
    randomIdsInternal(
      randomIds,
      Map("Hero" -> 4, "Weapon" -> 3, "Item" -> 2, "Spell" -> 3, "Villager" -> 3, "Village" -> 12),
      Nil)
  }

  def build(startingCardNames: List[String], cardDao: CardDao)(implicit ec: ExecutionContext): Future[Village] = {
    val eventualHeroRows = cardDao.getHeros
    val eventualSpellRows = cardDao.getSpells
    val eventualWeaponRows = cardDao.getWeapons
    val eventualItemRows = cardDao.getItems
    val eventualVillagerRows = cardDao.getVilliagers

    val eventualCardIds: Future[List[Int]] = for {
      heroRows <- eventualHeroRows
      spellRows <- eventualSpellRows
      weaponRows <- eventualWeaponRows
      itemRows <- eventualItemRows
      villagerRows <- eventualVillagerRows
    } yield drawRandomCardIds(
      heroRows.map(h => h.cardId -> h).toMap,
      spellRows.map(s => s.cardId -> s).toMap,
      weaponRows.map(w => w.cardId -> w).toMap,
      itemRows.map(i => i.cardId -> i).toMap,
      villagerRows.map(v => v.cardId -> v).toMap
    )
    val eventualStartingCardIds: Future[Seq[Int]] = cardDao.findByNames(startingCardNames).map(list => {
      list.map(_.cardId)
    })
    for {
      startingCardIds <- eventualStartingCardIds
      cardIds <- eventualCardIds
      v <- Village(startingCardIds, cardIds, cardDao)
    } yield v
  }
}

