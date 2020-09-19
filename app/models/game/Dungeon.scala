package models.game

import dao.CardDao
import play.api.libs.json.{Format, Json}

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

case class Dungeon(monsterPile: List[Card]) {

}

object Dungeon {
  implicit val dungeonFormat: Format[Dungeon] = Json.format[Dungeon]

  def getMonstersFromTypes(chosenTypes: Seq[(MonsterCard, Int)]): List[MonsterCard] = {
    chosenTypes.flatMap({
      case (monster, frequency) => List.fill(frequency)(monster)
    }).toList
  }

  def build(cardDao: CardDao)(implicit ec: ExecutionContext): Future[Dungeon] = {
    val eventualMonsterTypes: Future[Map[String, Seq[(MonsterCard, Int)]]] = cardDao.getMonstersByType
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
