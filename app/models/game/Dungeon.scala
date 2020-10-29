package models.game

import controllers.game.stage.PlayerDiscard
import dao.CardDao
import play.api.libs.json.{Format, JsError, JsObject, JsPath, JsSuccess, Json, Reads, Writes}

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

case class Dungeon(monsterPile: List[Card], ranks: List[Option[Card]]) {
  def hasMonster(index: Int): Boolean = ranks.drop(index - 1).head match {
    case Some(_: MonsterCard) => true
    case _ => false
  }

  def banish(rank: Int): Dungeon = {
    val monster = ranks.drop(rank - 1).head.get
    val newRanks = ranks.take(rank - 1) ::: None :: ranks.drop(rank)
    copy(monsterPile = monsterPile ::: monster :: Nil, ranks = newRanks)
  }

  def breachEffect(state: State): State =
    ranks.head match {
      case Some(monster: MonsterCard) => monster.breachEffect match {
        case Some(DestroyTwoHeroesFromVillagePiles) =>
          val newHeroPiles = state.village.get.heroes.map(p => p.copy(cards = p.cards.drop(2)))
          state.copy(village = state.village.map(v => v.copy(heroes = newHeroPiles)))
        case Some(DiscardTwoCards) => state.copy(currentStage =
          PlayerDiscard(
            currentPlayerId = state.currentPlayer.get.userId,
            playerIds = state.players.map(_.userId).toSet,
            howMany = 2,
            isBreach = true
          )
        )
        case None => state
      }
      case _ => state
    }

  def defeat(rank: Int): Dungeon = {
    val newRanks = ranks.take(rank - 1) ::: None :: ranks.drop(rank)
    copy(ranks = newRanks)
  }

  def lightPenalty: Int = 2

  def fill(state: State): State = {
    @tailrec
    def fillRanks(ranks: List[Option[Card]], pile: List[Card], finalRanks: List[Option[Card]]): Dungeon = {
      ranks match {
        case Nil => Dungeon(pile, finalRanks)
        case Some(card) :: remaining => fillRanks(remaining, pile, finalRanks ::: Some(card) :: Nil)
        case None :: remaining if pile.isEmpty => fillRanks(remaining, pile, finalRanks)
        case None :: remaining => fillRanks(remaining ::: (Some(pile.head) :: Nil), pile.drop(1), finalRanks)
      }
    }
    val newDungeon = fillRanks(ranks, monsterPile, Nil)
    if (ranks.head.isEmpty) {
      breachEffect(state.copy(dungeon = Some(newDungeon)))
    } else {
      state.copy(dungeon = Some(newDungeon))
    }
  }
}

object Dungeon {
  implicit val dungeonFormat: Format[Dungeon] = Format[Dungeon](
    Reads { js =>
      val monsterPile = (JsPath \ "monsterPile").read[List[Card]].reads(js)
      monsterPile.fold(
        _ => JsError("error reading monsters"), pile => {
          val ranks = (JsPath \ "ranks").read[List[Card]].reads(js)
          ranks.fold(
            _ => JsError("error reading ranks"), r => {
              JsSuccess(Dungeon(pile, r.map({
                case CardBack => None
                case c => Some(c)
              })))
            }
          )
        }
      )
    },
    Writes (d => {
      JsObject(
        Seq(
          "monsterPile" -> Json.toJson(d.monsterPile),
          "ranks" -> Json.toJson(d.ranks.map{
            case Some(c) => c
            case None => CardBack
          })
        )
      )
    })
  )
  def getMonstersFromTypes(chosenTypes: Seq[MonsterCard]): List[MonsterCard] = {
    chosenTypes.flatMap(monster => List.fill(monster.frequency)(monster)).toList
  }

  def build(cardDao: CardDao)(implicit ec: ExecutionContext): Future[Dungeon] = {
    val eventualMonsterTypes: Future[Map[String, Seq[MonsterCard]]] = cardDao.getMonstersByType
    val eventualThunderstoneRow = cardDao.findByNames(List("Stone Of Mystery"))
    val random = new Random
    for {
      monsterTypes <- eventualMonsterTypes
      thunderstoneRow <- eventualThunderstoneRow
    } yield {
      val chosenTypes = random.shuffle(monsterTypes.keys.toList).take(3)
      val monsters = getMonstersFromTypes(chosenTypes.flatMap(monsterTypes(_)))
      val shuffledMonsters = random.shuffle(monsters)
      val thunderstoneIndex = random.between(0, 11)
      val thunderstoneList  = ThunderstoneCard(thunderstoneRow.head) :: shuffledMonsters.drop(20 + thunderstoneIndex)
      new Dungeon(
        shuffledMonsters.slice(3, thunderstoneIndex + 20) ::: thunderstoneList,
        shuffledMonsters.take(3).map(m => Some(m))
      )
      new Dungeon(
        shuffledMonsters.slice(3, thunderstoneIndex + 23) ::: thunderstoneList,
        shuffledMonsters.take(3).map(m => Some(m))
      )
    }
  }
}
