package models.game

import dao.CardDao
import play.api.libs.json.{Format, Json}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

case class Dungeon(monsterPile: List[Card]) {
  def fightOff(rank: Int): Dungeon = {
    val newPile = monsterPile.take(rank - 1) ++ monsterPile.drop(rank) ++ (monsterPile.drop(rank - 1).head :: Nil)
    copy(monsterPile = newPile)
  }

  def defeat(rank: Int): Dungeon = {
    copy(monsterPile = monsterPile.take(rank - 1) ++ monsterPile.drop(rank))
  }

  def lightPenalty: Int = 2
}

object Dungeon {
  implicit val dungeonFormat: Format[Dungeon] = Json.format[Dungeon]

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
      new Dungeon(shuffledMonsters.take(20 + thunderstoneIndex) ::: thunderstoneList)
    }
  }
}
