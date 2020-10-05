package models.game

import dao.CardDao
import models.schema.Tables.{HeroRow, ItemRow, SpellRow, VillagerRow, WeaponRow}
import play.api.libs.json.{Json, OFormat}
import services.CardManager.removeOneInstanceFromCards

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

case class Village(
                    heroes: List[HeroPile],
                    weapons: List[WeaponPile],
                    items: List[ItemPile],
                    spells: List[SpellPile],
                    villagers: List[VillagerPile],
                    diseases: DiseasePile
             ) {
  def takeCard(cardName: String): (Village, Option[Card]) = {
    var foundCard: Option[Card] = None
    val newHeroes = heroes.map(hp => {
      hp.cards.find(p => p.name == cardName) match {
        case Some(card) =>
          foundCard = Some(card)
          new HeroPile(removeOneInstanceFromCards(hp.cards, card))
        case None => hp
      }
    })
    val newWeapons = weapons.map(wp => {
      wp.cards.find(p => p.name == cardName) match {
        case Some(card) =>
          foundCard = Some(card)
          new WeaponPile(removeOneInstanceFromCards(wp.cards, card))
        case None => wp
      }
    })
    val newItems = items.map(ip => {
      ip.cards.find(p => p.name == cardName) match {
        case Some(card) =>
          foundCard = Some(card)
          new ItemPile(removeOneInstanceFromCards(ip.cards, card))
        case None => ip
      }
    })
    val newSpells = spells.map(sp => {
      sp.cards.find(p => p.name == cardName) match {
        case Some(card) =>
          foundCard = Some(card)
          new SpellPile(removeOneInstanceFromCards(sp.cards, card))
        case None => sp
      }
    })
    val newVillagers = villagers.map(vp => {
      vp.cards.find(p => p.name == cardName) match {
        case Some(card) =>
          foundCard = Some(card)
          new VillagerPile(removeOneInstanceFromCards(vp.cards, card))
        case None => vp
      }
    })
    val newDiseases = diseases.cards.find(p => p.name == cardName) match {
      case Some(card) =>
        foundCard = Some(card)
        new DiseasePile(removeOneInstanceFromCards(diseases.cards, card))
      case None => diseases
    }
    (new Village(newHeroes, newWeapons, newItems, newSpells, newVillagers, newDiseases), foundCard)
  }
}

object Village {
  implicit val villageFormat: OFormat[Village] = Json.format[Village]
  def apply(startingCardIds: Seq[Int], cardIds: Seq[Int], cardDao: CardDao)
           (implicit ec: ExecutionContext): Future[Village] = {
    val eventualCards: Future[Map[Int, Card]] = cardDao.findByIds((startingCardIds ++ cardIds).toList)
    eventualCards.map(cards => {
      val heroesByType = cards.collect({
        case (_, h: HeroCard) => h
      }).groupBy(h => h.heroType)

      val piles = (startingCardIds ++ cardIds).map(id => cards(id) match {
        case h: HeroCard => HeroPile(heroesByType(h.heroType))
        case i: ItemCard => ItemPile(i)
        case s: SpellCard => SpellPile(s)
        case v: VillagerCard => VillagerPile(v)
        case w: WeaponCard => WeaponPile(w)
        case d: DiseaseCard => DiseasePile(d)
      })
      new Village(
        piles.collect{case h: HeroPile => h}.toList,
        piles.collect{case w: WeaponPile => w}.toList,
        piles.collect{case i: ItemPile => i}.toList,
        piles.collect{case s: SpellPile => s}.toList,
        piles.collect{case v: VillagerPile => v}.toList,
        piles.collect{case d: DiseasePile => d}.head
      )
    })
  }

  private def isVillage(s: String) = {
    Set("Villager", "Item", "Spell", "Weapon").contains(s)
  }

  private def isHero(s: String) = s == "Hero"

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
        case t if isVillage(t) && cardLimits(t) > 0 && cardLimits("Village") > 0 =>
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
      Map("Hero" -> 4, "Weapon" -> 3, "Item" -> 2, "Spell" -> 3, "Villager" -> 3, "Village" -> 8),
      Nil)
  }

  def build(startingCardNames: List[String], cardDao: CardDao)(implicit ec: ExecutionContext): Future[Village] = {
    val eventualHeroRows = cardDao.getHeros
    val eventualSpellRows = cardDao.getSpells
    val eventualWeaponRows = cardDao.getWeapons
    val eventualItemRows = cardDao.getItems
    val eventualVillagerRows = cardDao.getVilliagers
    val eventualStartingCardIds: Future[Seq[Int]] = cardDao.findByNames(startingCardNames).map(list => {
      list.map(_.cardId)
    })
    val eventualCardIds: Future[List[Int]] = for {
      heroRows <- eventualHeroRows
      spellRows <- eventualSpellRows
      weaponRows <- eventualWeaponRows
      itemRows <- eventualItemRows
      villagerRows <- eventualVillagerRows
      startingCardIds <- eventualStartingCardIds
    } yield {
      val startingCardIdSet =startingCardIds.toSet
      drawRandomCardIds(
        heroRows.filterNot(h => startingCardIdSet.contains(h.cardId)).map(h => h.cardId -> h).toMap,
        spellRows.map(s => s.cardId -> s).toMap,
        weaponRows.filterNot(w => startingCardIdSet.contains(w.cardId)).map(w => w.cardId -> w).toMap,
        itemRows.filterNot(i => startingCardIdSet.contains(i.cardId)).map(i => i.cardId -> i).toMap,
        villagerRows.map(v => v.cardId -> v).toMap
      )
    }
    for {
      startingCardIds <- eventualStartingCardIds
      cardIds <- eventualCardIds
      v <- Village(startingCardIds, cardIds, cardDao)
    } yield v
  }
}

