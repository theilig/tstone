package services

import com.google.inject.Inject
import dao.CardDao
import models.game.State
import models.schema.Tables.{HeroRow, ItemRow, SpellRow, VillagerRow, WeaponRow}

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class GameSetup @Inject() (cardDao: CardDao)(implicit ec: ExecutionContext) {
  def setupGame(state: State): Future[State] = {
    val random = new Random
    dealStartingCards(state).flatMap(state => buildVillage(state)).map(state => {
      setCurrentPlayer(random.between(0, state.players.length))

    })
  }

  def dealStartingCards(state: State): Future[State] = {
    val deck: Map[String, Int] = Map("Militia" -> 6, "Iron Rations" -> 2, "Dagger" -> 2, "Torch" -> 2)
    val eventualStartingCards = cardDao.findByNames(deck.keys.toList)
    eventualStartingCards.map(startingCards => {
      val startingDeck = (for {
        cardRow <- startingCards
        number <- deck(cardRow.name)
      } yield List.fill(number, Card(cardRow))).flatten
      val newPlayers = state.players.map(p =>
        p.copy(discard = startingDeck)
      )
      state.copy(players = newPlayers)
    }
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

  def buildVillage(state: State): Future[State] = {
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

  }
}