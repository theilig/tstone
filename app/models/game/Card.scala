package models.game

import java.sql.Connection

import play.api.libs.json._

class Card(name: String, imageName: String) {
  def light: Int = 0
  def victoryPoints: Int = 0
  def goldValue = 0
  def write(connection: Connection): Unit = {
    val statement = connection.createStatement()
    statement.execute(s"INSERT INTO Card (name, image) VALUES ('$name', '$imageName')")
    statement.close()
  }
}

object Card {
  implicit val cardFormat: OFormat[Card] = Json.format[Card]

  def apply(line: String): Card = {
    val fields = line.split("""\s+""")
    val name = fields.tail.takeWhile(f => !f.contains(":")).mkString(" ")
    val attributes = fields.dropWhile(f => !f.contains(":")).map(f => {
      val pair = f.split(':')
      if (pair.length < 2) {
        println(s"Can't parse $line")
      }
      (pair(0), pair(1))
    }).toMap
    new Card(name, s"card${fields.head}.png")
  }
}

class VillageCard(name: String, imageName: String, cost: Int) extends Card(name, imageName)

object VillageCard {
  implicit val villageCardWrites: Writes[VillageCard] = (card: VillageCard) => Card.cardFormat.writes(card)
  implicit val villageCardReads: Reads[VillageCard] = (js: JsValue) => {
    val cResult = Card.cardFormat.reads(js)
    cResult match {
      case JsSuccess(card, path) => JsSuccess(new VillageCard(card.name, card.imageName), path)
      case e: JsError => e
    }
  }
}

case class HeroCard(
                     name: String,
                     imageName: String,
                     light: Int,
                     level: Int,
                     strength: Int,
                     cost: Int,
                     traits: List[String],
                     goldValue: Option[Int],
                     victoryPoints: Int
                   ) extends VillageCard(name, imageName, cost)

case class ItemCard(
                     name: String,
                     imageName: String,
                     light: Int,
                     cost: Int,
                     traits: List[String],
                     goldValue: Option[Int],
                     victoryPoints: Int
                   ) extends VillageCard(name, imageName, cost)

case class SpellCard(
                      name: String,
                      imageName: String,
                      light: Int,
                      cost: Int,
                      traits: List[Int],
                      victoryPoints: Int
                    ) extends VillageCard(name, imageName, cost)

case class VillagerCard(
                         name: String,
                         imageName: String,
                         cost: Int,
                         traits: List[String],
                         goldValue: Option[Int],
                         victoryPoints: Int
                       ) extends VillageCard(name, imageName, cost)

case class WeaponCard(
                      name: String,
                      imageName: String,
                      light: Int,
                      weight: Int,
                      cost: Int,
                      traits: List[String],
                      goldValue: Option[Int],
                      victoryPoints: Int
                    ) extends VillageCard(name, imageName, cost)

class DungeonCard(name: String, imageName: String) extends Card(name, imageName)

object DungeonCard {
  implicit val dungeonCardWrites: Writes[DungeonCard] = (card: DungeonCard) => Card.cardFormat.writes(card)
  implicit val dungeonCardReads: Reads[DungeonCard] = (js: JsValue) => {
    val cResult = Card.cardFormat.reads(js)
    cResult match {
      case JsSuccess(card, path) => JsSuccess(new DungeonCard(card.name, card.imageName), path)
      case e: JsError => e
    }
  }
}

case class MonsterCard(
                        name: String,
                        imageName: String,
                        light: Int,
                        health: Int,
                        traits: List[String],
                        goldValue: Option[Int],
                        victoryPoints: Int
                      ) extends DungeonCard(name, imageName)

case class ThunderstoneCard(
                            name: String,
                            imageName: String,
                            victoryPoints: Int
                      ) extends DungeonCard(name, imageName)



