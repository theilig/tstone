package models.game

import dao.CardDao

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class Dungeon(monsterPile: List[DungeonCard]) {

}

object Dungeon {

  def getMonstersFromTypes(chosenTypes: Map[String, Seq[MonsterCard]]): Seq[MonsterCard] = ???

  def build(cardDao: CardDao)(implicit ec: ExecutionContext): Future[Dungeon] = {
    val eventualMonsterTypes: Future[Map[String, Seq[MonsterCard]]] = cardDao.getMonstersByType
    val eventualThunderStoneRow = cardDao.findByNames(List("Stone Of Mystery"))
    val random = new Random
    for {
      monsterTypes <- eventualMonsterTypes
      thunderStoneRow <- eventualThunderStoneRow
    } yield {
      val chosenTypes = random.shuffle(monsterTypes.take(3))
      val monsters = getMonstersFromTypes(chosenTypes).toList
      val shuffledMonsters: List[DungeonCard] = random.shuffle(monsters)
      val thunderStoneIndex = random.between(0, 11)
      val thunderStoneList: List[DungeonCard] = ThunderstoneCard(thunderStoneRow.head) :: shuffledMonsters.drop(20 + thunderStoneIndex)
      new Dungeon(shuffledMonsters.take(20 + thunderStoneIndex) ::: thunderStoneList)
    }
  }
}
