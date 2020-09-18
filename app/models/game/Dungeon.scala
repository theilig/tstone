package models.game

import dao.CardDao
import play.api.libs.json.{Format, Json}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

case class Dungeon(monsterPile: List[Card]) {

}

object Dungeon {
  implicit val dungeonFormat: Format[Dungeon] = Json.format[Dungeon]

  def getMonstersFromTypes(chosenTypes: Map[String, Seq[MonsterCard]]): Seq[MonsterCard] = ???

  def build(cardDao: CardDao)(implicit ec: ExecutionContext): Future[Dungeon] = {
    val eventualMonsterTypes: Future[Map[String, Seq[MonsterCard]]] = cardDao.getMonstersByType
    val eventualThunderstoneRow = cardDao.findByNames(List("Stone Of Mystery"))
    val random = new Random
    for {
      monsterTypes <- eventualMonsterTypes
      thunderstoneRow <- eventualThunderstoneRow
    } yield {
      val chosenTypes = random.shuffle(monsterTypes.take(3))
      val monsters = getMonstersFromTypes(chosenTypes).toList
      val shuffledMonsters: List[Card] = random.shuffle(monsters)
      val thunderstoneIndex = random.between(0, 11)
      val thunderstoneList: List[Card] = ThunderstoneCard(thunderstoneRow.head) :: shuffledMonsters.drop(20 + thunderstoneIndex)
      new Dungeon(shuffledMonsters.take(20 + thunderstoneIndex) ::: thunderstoneList)
    }
  }
}
