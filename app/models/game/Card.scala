package models.game

import java.sql.Connection

import models.CardInfo
import play.api.libs.json._
import models.schema.Tables._

class Card(id: Int, name: String, imageName: String) {
  def getLight: Int = 0
  def getVictoryPoints: Int = 0
  def getGoldValue = 0
  def getName: String = name
  def getImageName: String = imageName
  def write(connection: Connection): Unit = {
    val statement = connection.createStatement()
    statement.execute(s"INSERT INTO Card (name, image) VALUES ('$name', '$imageName')")
    statement.close()
  }
}

object Card {
  implicit val cardFormat: Format[Card] = Format[Card](
    Reads { js =>
      val cardType = (JsPath \ "cardType").read[String].reads(js)
      cardType.fold(
        _ => JsError("cardType undefined or incorrect"), {
          case "HeroCard" => (JsPath \ "data").read[HeroCard].reads(js)
          case "ItemCard" => (JsPath \ "data").read[ItemCard].reads(js)
          case "MonsterCard" => (JsPath \ "data").read[MonsterCard].reads(js)
          case "SpellCard" => (JsPath \ "data").read[SpellCard].reads(js)
          case "ThunderstoneCard" => (JsPath \ "data").read[ThunderstoneCard].reads(js)
          case "WeaponCard" => (JsPath \ "data").read[WeaponCard].reads(js)
          case "VillagerCard" => (JsPath \ "data").read[VillagerCard].reads(js)
        }
      )
    },
    Writes {
      case h: HeroCard => JsObject(Seq("cardType" -> JsString("HeroCard"), "data" -> HeroCard.heroFormat.writes(h)))
      case i: ItemCard => JsObject(Seq("cardType" -> JsString("ItemCard"), "data" -> ItemCard.itemFormat.writes(i)))
      case m: MonsterCard => JsObject(Seq("cardType" -> JsString("MonsterCard"), "data" -> MonsterCard.monsterFormat.writes(m)))
      case s: SpellCard => JsObject(Seq("cardType" -> JsString("SpellCard"), "data" -> SpellCard.spellFormat.writes(s)))
      case t: ThunderstoneCard => JsObject(Seq("cardType" -> JsString("ThunderstoneCard"), "data" -> ThunderstoneCard.thunderstoneFormat.writes(t)))
      case w: WeaponCard => JsObject(Seq("cardType" -> JsString("WeaponCard"), "data" -> WeaponCard.weaponFormat.writes(w)))
      case v: VillagerCard => JsObject(Seq("cardType" -> JsString("VillagerCard"), "data" -> VillagerCard.villagerFormat.writes(v)))
    }
  )
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
    val image = s"card${fields.head}.png"
    if (attributes.contains("str")) {
      HeroCard(name, image, attributes)
    } else if (attributes.getOrElse("t", "").contains("Item")) {
      ItemCard(name, image, attributes)
    } else if (attributes.getOrElse("t", "").contains("Weapon")) {
      WeaponCard(name, image, attributes)
    } else if (attributes.contains("health")) {
      MonsterCard(name, image, attributes)
    } else {
      new Card(0, name, image)
    }
  }

  def apply(info: CardInfo): Card = {
    if (info.heroRow.nonEmpty) {
      HeroCard(info.cardRow, info.heroRow.get, info.heroClasses, info.battleEffects, info.dungeonEffects)
    } else if (info.itemRow.nonEmpty) {
      ItemCard(info.cardRow, info.itemRow.get, info.itemTraits, info.dungeonEffects, info.villageEffects)
    } else if (info.monsterRow.nonEmpty) {
      MonsterCard(
        info.cardRow,
        info.monsterRow.get,
        info.monsterType,
        info.battleEffects,
        info.dungeonEffects,
        info.breachEffects
      )
    } else if (info.spellRow.nonEmpty) {
      SpellCard(info.cardRow, info.spellRow.get, info.itemTraits, info.battleEffects, info.dungeonEffects)
    } else if (info.weaponRow.nonEmpty) {
      WeaponCard(info.cardRow, info.weaponRow.get, info.itemTraits, info.dungeonEffects)
    } else if (info.villagerRow.nonEmpty) {
      VillagerCard(info.cardRow, info.villagerRow.get, info.villageEffects)
    } else {
      throw new MatchError("Card did not match known types")
    }
  }
}


case class HeroCard(
                     id: Int,
                     name: String,
                     imageName: String,
                     light: Int,
                     level: Int,
                     strength: Int,
                     cost: Int,
                     traits: List[String],
                     heroType: String,
                     goldValue: Option[Int],
                     victoryPoints: Int
                   ) extends Card(id, name, imageName)

object HeroCard {
  implicit val heroFormat: Format[HeroCard] = Json.format[HeroCard]

  def apply(cardRow: CardRow, heroRow: HeroRow, classes: Seq[HeroClassRow], battleEffects: Seq[BattleEffectRow],
            dungeonEffects: Seq[DungeonEffectRow]): HeroCard = {
    new HeroCard(cardRow.cardId, cardRow.name, cardRow.image, heroRow.light, heroRow.level, heroRow.strength, heroRow.cost,
      classes.map(_.`trait`).toList, heroRow.heroType, heroRow.goldValue,
      heroRow.victoryPoints)
  }
  def apply(name: String, image: String, attributes: Map[String, String]): HeroCard = {
    new HeroCard(0, name, image, attributes.getOrElse("li", "0").toInt, attributes.getOrElse("lv", "0").toInt,
      attributes("str").toInt, attributes("c").toInt,
      attributes.getOrElse("t", "").split(",").toList.filterNot(_ == ""),
      name.split(" ")(0), attributes.get("gv").map(_.toInt), attributes.getOrElse("vp", "0").toInt)
  }
}

case class ItemCard(
                     id: Int,
                     name: String,
                     imageName: String,
                     light: Int,
                     cost: Int,
                     traits: List[String],
                     goldValue: Int,
                     victoryPoints: Int
                   ) extends Card(id, name, imageName)

object ItemCard {
  implicit val itemFormat: Format[ItemCard] = Json.format[ItemCard]
  def apply(cardRow: CardRow, itemRow: ItemRow, itemTraits: Seq[ItemTraitRow], dungeonEffects: Seq[DungeonEffectRow],
            villageEffects: Seq[VillageEffectRow]): ItemCard = {
    new ItemCard(cardRow.cardId, cardRow.name, cardRow.image, itemRow.light, itemRow.cost,
      itemTraits.map(_.`trait`).toList, itemRow.goldValue, itemRow.victoryPoints)
  }
  def apply(name: String, image: String, attributes: Map[String, String]): ItemCard = {
    new ItemCard(0, name, image, attributes.getOrElse("li", "0").toInt, attributes("c").toInt,
      attributes.getOrElse("t", "").split(",").toList.filterNot(_ == ""), attributes("gv").toInt,
      attributes.getOrElse("vp", "0").toInt)
  }
}

case class SpellCard(
                      id: Int,
                      name: String,
                      imageName: String,
                      light: Int,
                      cost: Int,
                      traits: List[String],
                      victoryPoints: Int
                    ) extends Card(id, name, imageName)
object SpellCard {
  implicit val spellFormat: Format[SpellCard] = Json.format[SpellCard]
  def apply(cardRow: CardRow, spellRow: SpellRow, itemTraits: Seq[ItemTraitRow], battleEffects: Seq[BattleEffectRow],
            dungeonEffects: Seq[DungeonEffectRow]): SpellCard = {
    new SpellCard(cardRow.cardId, cardRow.name, cardRow.image, spellRow.light, spellRow.cost,
      itemTraits.map(_.`trait`).toList, spellRow.victoryPoints)
  }
}

case class VillagerCard(
                         id: Int,
                         name: String,
                         imageName: String,
                         cost: Int,
                         traits: List[String],
                         goldValue: Option[Int],
                         victoryPoints: Int
                       ) extends Card(id, name, imageName)

object VillagerCard {
  implicit val villagerFormat: Format[VillagerCard] = Json.format[VillagerCard]
  def apply(cardRow: CardRow, villagerRow: VillagerRow, villageEffects: Seq[VillageEffectRow]): VillagerCard = {
    new VillagerCard(cardRow.cardId, cardRow.name, cardRow.image, villagerRow.cost, Nil, villagerRow.goldValue,
      villagerRow.victoryPoints)
  }
}

case class WeaponCard(
                       id: Int,
                       name: String,
                       imageName: String,
                       light: Int,
                       weight: Int,
                       cost: Int,
                       traits: List[String],
                       goldValue: Int,
                       victoryPoints: Int
                    ) extends Card(id, name, imageName)

object WeaponCard {
  implicit val weaponFormat: Format[WeaponCard] = Json.format[WeaponCard]
  def apply(cardRow: CardRow, weaponRow: WeaponRow, itemTraits: Seq[ItemTraitRow],
            dungeonEffects: Seq[DungeonEffectRow]): WeaponCard = {
    new WeaponCard(cardRow.cardId, cardRow.name, cardRow.image, weaponRow.light, weaponRow.weight, weaponRow.cost,
      itemTraits.map(_.`trait`).toList, weaponRow.goldValue, weaponRow.victoryPoints)
  }
  def apply(name: String, image: String, attributes: Map[String, String]): WeaponCard = {
    new WeaponCard(0, name, image, attributes.getOrElse("li", "0").toInt, attributes.getOrElse("w", "0").toInt,
      attributes("c").toInt, attributes.getOrElse("t", "").split(",").toList.filterNot(_ == ""),
      attributes.getOrElse("gv", "0").toInt, attributes.getOrElse("vp", "0").toInt)
  }
}

case class MonsterCard(
                        id: Int,
                        name: String,
                        imageName: String,
                        light: Int,
                        health: Int,
                        traits: List[String],
                        goldValue: Int,
                        victoryPoints: Int,
                        experiencePoints: Int
                      ) extends Card(id, name, imageName)

object MonsterCard {
  implicit val monsterFormat: Format[MonsterCard] = Json.format[MonsterCard]
  def apply(cardRow: CardRow, monsterRow: MonsterRow, monsterTypes: Seq[MonsterTypeRow],
            battleEffects: Seq[BattleEffectRow], dungeonEffects: Seq[DungeonEffectRow],
            breachEffects: Seq[BreachEffectRow]): MonsterCard = {
    new MonsterCard(cardRow.cardId, cardRow.name, cardRow.image, monsterRow.light, monsterRow.health,
      monsterTypes.map(_.`trait`).toList, monsterRow.goldValue, monsterRow.victoryPoints, monsterRow.experience)
  }
  def apply(name: String, image: String, attributes: Map[String, String]): MonsterCard = {
    new MonsterCard(0, name, image, attributes.getOrElse("li", "0").toInt, attributes("health").toInt,
      attributes.getOrElse("t", "").split(",").toList.filterNot(_ == ""),
      attributes("gv").toInt, attributes("vp").toInt, attributes("xp").toInt)
  }

}

case class ThunderstoneCard(
                            id: Int,
                            name: String,
                            imageName: String,
                            victoryPoints: Int
                      ) extends Card(id, name, imageName)
object ThunderstoneCard {
  implicit val thunderstoneFormat: Format[ThunderstoneCard] = Json.format[ThunderstoneCard]
  def apply(row: CardRow): ThunderstoneCard = {
    new ThunderstoneCard(row.cardId, row.name, row.image, 3)
  }
}



