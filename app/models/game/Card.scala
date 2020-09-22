package models.game

import java.sql.Connection

import models.CardInfo
import play.api.libs.json._
import models.schema.Tables

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
          case "DiseaseCard" => (JsPath \ "data").read[DiseaseCard].reads(js)
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
      case d: DiseaseCard => JsObject(Seq("cardType" -> JsString("DiseaseCard"), "data" -> DiseaseCard.diseaseFormat.writes(d)))
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
    } else if (attributes.getOrElse("t", "").contains("Spell")) {
      SpellCard(name, image, attributes)
    } else if (attributes.getOrElse("t", "").contains("Villager")) {
      VillagerCard(name, image, attributes)
    } else if (name == "Disease") {
      new DiseaseCard(0, name, image,
        TurnEffect.parse("Dungeon", attributes("de")).asInstanceOf[List[DungeonEffect]])
    } else {
      new ThunderstoneCard(0, name, image, attributes("vp").toInt)
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
    } else if (info.cardRow.name == "Disease") {
      DiseaseCard(info.cardRow, info.dungeonEffects)
    } else if (info.thunderstones.nonEmpty) {
      ThunderstoneCard(
        info.cardRow.cardId,
        info.cardRow.name,
        info.cardRow.image,
        info.thunderstones.head.victoryPoints)
    } else {
      throw new MatchError("Card did not match known types")
    }
  }
}

case class DiseaseCard(
                         id: Int,
                         name: String,
                         imageName: String,
                         dungeonEffects: List[DungeonEffect],
                       ) extends Card(id, name, imageName)

object DiseaseCard {
  implicit val diseaseFormat: Format[DiseaseCard] = Json.format[DiseaseCard]
  def apply(cardRow: Tables.CardRow, dungeonEffects: Seq[Tables.DungeonEffectRow]): DiseaseCard = {
    new DiseaseCard(cardRow.cardId, cardRow.name, cardRow.image,
      dungeonEffects.map(DungeonEffect(_)).toList)
  }
  def apply(name: String, image: String, attributes: Map[String, String]): DiseaseCard = {
    new DiseaseCard(0, name, image,
      TurnEffect.parse("Dungeon", attributes.getOrElse("de", "")).asInstanceOf[List[DungeonEffect]]
    )
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
                     battleEffects: List[BattleEffect],
                     dungeonEffects: List[DungeonEffect],
                     goldValue: Option[Int],
                     victoryPoints: Int
                   ) extends Card(id, name, imageName)

object HeroCard {
  implicit val heroFormat: Format[HeroCard] = Json.format[HeroCard]

  def apply(cardRow: Tables.CardRow, heroRow: Tables.HeroRow, classes: Seq[Tables.HeroClassRow],
            battleEffects: Seq[Tables.BattleEffectRow], dungeonEffects: Seq[Tables.DungeonEffectRow]): HeroCard = {
    new HeroCard(cardRow.cardId, cardRow.name, cardRow.image, heroRow.light, heroRow.level, heroRow.strength, heroRow.cost,
      classes.map(_.`trait`).toList, heroRow.heroType, battleEffects.map(BattleEffect(_)).toList,
      dungeonEffects.map(DungeonEffect(_)).toList, heroRow.goldValue, heroRow.victoryPoints)
  }
  def apply(name: String, image: String, attributes: Map[String, String]): HeroCard = {
    new HeroCard(0, name, image, attributes.getOrElse("li", "0").toInt, attributes.getOrElse("lv", "0").toInt,
      attributes("str").toInt, attributes("c").toInt,
      attributes.getOrElse("t", "").split(",").toList.filterNot(_ == ""),
      name.split(" ")(0),
      TurnEffect.parse("Battle", attributes.getOrElse("be", "")).asInstanceOf[List[BattleEffect]],
      TurnEffect.parse("Dungeon", attributes.getOrElse("de", "")).asInstanceOf[List[DungeonEffect]],
      attributes.get("gv").map(_.toInt), attributes.getOrElse("vp", "0").toInt)
  }
}

case class ItemCard(
                     id: Int,
                     name: String,
                     imageName: String,
                     light: Int,
                     cost: Int,
                     traits: List[String],
                     dungeonEffects: List[DungeonEffect],
                     goldValue: Int,
                     victoryPoints: Int
                   ) extends Card(id, name, imageName)

object ItemCard {
  implicit val itemFormat: Format[ItemCard] = Json.format[ItemCard]
  def apply(cardRow: Tables.CardRow, itemRow: Tables.ItemRow, itemTraits: Seq[Tables.ItemTraitRow],
            dungeonEffects: Seq[Tables.DungeonEffectRow], villageEffects: Seq[Tables.VillageEffectRow]): ItemCard = {
    new ItemCard(cardRow.cardId, cardRow.name, cardRow.image, itemRow.light, itemRow.cost,
      itemTraits.map(_.`trait`).toList, dungeonEffects.map(DungeonEffect(_)).toList, itemRow.goldValue,
      itemRow.victoryPoints)
  }
  def apply(name: String, image: String, attributes: Map[String, String]): ItemCard = {
    new ItemCard(0, name, image, attributes.getOrElse("li", "0").toInt, attributes("c").toInt,
      attributes.getOrElse("t", "").split(",").toList.filterNot(_ == ""),
      TurnEffect.parse("Dungeon", attributes.getOrElse("de", "")).asInstanceOf[List[DungeonEffect]],
      attributes("gv").toInt, attributes.getOrElse("vp", "0").toInt)
  }
}

case class SpellCard(
                      id: Int,
                      name: String,
                      imageName: String,
                      light: Int,
                      cost: Int,
                      traits: List[String],
                      dungeonEffects: List[DungeonEffect],
                      victoryPoints: Int
                    ) extends Card(id, name, imageName)
object SpellCard {
  implicit val spellFormat: Format[SpellCard] = Json.format[SpellCard]
  def apply(cardRow: Tables.CardRow, spellRow: Tables.SpellRow, itemTraits: Seq[Tables.ItemTraitRow],
            battleEffects: Seq[Tables.BattleEffectRow], dungeonEffects: Seq[Tables.DungeonEffectRow]): SpellCard = {
    new SpellCard(cardRow.cardId, cardRow.name, cardRow.image, spellRow.light, spellRow.cost,
      itemTraits.map(_.`trait`).toList, dungeonEffects.map(DungeonEffect(_)).toList, spellRow.victoryPoints)
  }
  def apply(name: String, image: String, attributes: Map[String, String]): SpellCard = {
    new SpellCard(0, name, image, attributes.getOrElse("li", "0").toInt, attributes("c").toInt,
      attributes.getOrElse("t", "").split(",").toList.filterNot(_ == ""),
      TurnEffect.parse("Dungeon", attributes.getOrElse("de", "")).asInstanceOf[List[DungeonEffect]],
      attributes.getOrElse("vp", "0").toInt)
  }
}

case class VillagerCard(
                         id: Int,
                         name: String,
                         imageName: String,
                         cost: Int,
                         traits: List[String],
                         villageEffects: List[VillageEffect],
                         goldValue: Option[Int],
                         victoryPoints: Int
                       ) extends Card(id, name, imageName)

object VillagerCard {
  implicit val villagerFormat: Format[VillagerCard] = Json.format[VillagerCard]
  def apply(cardRow: Tables.CardRow, villagerRow: Tables.VillagerRow,
            villageEffects: Seq[Tables.VillageEffectRow]): VillagerCard = {
    new VillagerCard(cardRow.cardId, cardRow.name, cardRow.image, villagerRow.cost, Nil,
      villageEffects.map(VillageEffect(_)).toList, villagerRow.goldValue,
      villagerRow.victoryPoints)
  }
  def apply(name: String, image: String, attributes: Map[String, String]): VillagerCard = {
    new VillagerCard(0, name, image, attributes("c").toInt,
      attributes.getOrElse("t", "").split(",").toList.filterNot(_ == ""),
      TurnEffect.parse("Village", attributes.getOrElse("ve", "")).asInstanceOf[List[VillageEffect]],
      attributes.get("gv").map(_.toInt), attributes.getOrElse("vp", "0").toInt)
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
                       dungeonEffects: List[DungeonEffect],
                       goldValue: Int,
                       victoryPoints: Int
                    ) extends Card(id, name, imageName)

object WeaponCard {
  implicit val weaponFormat: Format[WeaponCard] = Json.format[WeaponCard]
  def apply(cardRow: Tables.CardRow, weaponRow: Tables.WeaponRow, itemTraits: Seq[Tables.ItemTraitRow],
            dungeonEffects: Seq[Tables.DungeonEffectRow]): WeaponCard = {
    new WeaponCard(cardRow.cardId, cardRow.name, cardRow.image, weaponRow.light, weaponRow.weight, weaponRow.cost,
      itemTraits.map(_.`trait`).toList, dungeonEffects.map(DungeonEffect(_)).toList, weaponRow.goldValue,
      weaponRow.victoryPoints)
  }
  def apply(name: String, image: String, attributes: Map[String, String]): WeaponCard = {
    new WeaponCard(0, name, image, attributes.getOrElse("li", "0").toInt, attributes.getOrElse("w", "0").toInt,
      attributes("c").toInt, attributes.getOrElse("t", "").split(",").toList.filterNot(_ == ""),
      TurnEffect.parse("Dungeon", attributes.getOrElse("de", "")).asInstanceOf[List[DungeonEffect]],
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
                        breachEffect:  Option[BreachEffect],
                        battleEffects: List[BattleEffect],
                        dungeonEffects: List[DungeonEffect],
                        goldValue: Int,
                        victoryPoints: Int,
                        experiencePoints: Int
                      ) extends Card(id, name, imageName)

object MonsterCard {
  implicit val monsterFormat: Format[MonsterCard] = Json.format[MonsterCard]
  def apply(cardRow: Tables.CardRow, monsterRow: Tables.MonsterRow, monsterTypes: Seq[Tables.MonsterTypeRow],
            battleEffects: Seq[Tables.BattleEffectRow], dungeonEffects: Seq[Tables.DungeonEffectRow],
            breachEffects: Seq[Tables.BreachEffectRow]): MonsterCard = {
    val breachEffect: Option[BreachEffect] = getBreachEffect(breachEffects.headOption.map(_.effect))
    new MonsterCard(cardRow.cardId, cardRow.name, cardRow.image, monsterRow.light, monsterRow.health,
      monsterTypes.map(_.`trait`).toList, breachEffect, battleEffects.map(BattleEffect(_)).toList,
      dungeonEffects.map(DungeonEffect(_)).toList, monsterRow.goldValue, monsterRow.victoryPoints, monsterRow.experience)
  }

  private def getBreachEffect(breachEffects: Option[String]): Option[BreachEffect] = {
    val breachEffect = breachEffects match {
      case Some(effect) if effect == "ReduceHeroes" =>
        Some(DestroyTwoHeroesFromVillagePiles)
      case Some(effect) if effect == "Discard2" =>
        Some(DiscardTwoCards)
      case None => None
    }
    breachEffect
  }

  def apply(name: String, image: String, attributes: Map[String, String]): MonsterCard = {
    new MonsterCard(0, name, image, attributes.getOrElse("li", "0").toInt, attributes("health").toInt,
      attributes.getOrElse("t", "").split(",").toList.filterNot(_ == ""),
      getBreachEffect(attributes.get("breach")),
      TurnEffect.parse("Battle", attributes.getOrElse("be", "")).asInstanceOf[List[BattleEffect]],
      TurnEffect.parse("Dungeon", attributes.getOrElse("de", "")).asInstanceOf[List[DungeonEffect]],
      attributes("gv").toInt, attributes("vp").toInt, attributes("xp").toInt)
  }
}

case class ThunderstoneCard( id: Int, name: String, imageName: String, victoryPoints: Int)
  extends Card(id, name, imageName)
object ThunderstoneCard {
  implicit val thunderstoneFormat: Format[ThunderstoneCard] = Json.format[ThunderstoneCard]
  def apply(row: Tables.CardRow): ThunderstoneCard = {
    new ThunderstoneCard(row.cardId, row.name, row.image, 3)
  }
}



