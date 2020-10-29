package models.game

import java.sql.Connection
import java.sql

import models.CardInfo
import play.api.libs.json._
import models.schema.Tables

class Card(name: String, imageName: String, val frequency: Int) {
  def canUpgrade(totalExperience: Int): Boolean =
    this match {
      case h: HeroCard => h.upgradeCost.exists(cost => cost <= totalExperience)
      case _ => false
    }
  def getLight: Int = 0
  def getGoldValue = 0
  def hasGoldValue: Boolean = false
  def getName: String = name
  def getCost: Option[Int] = None
  def getImageName: String = imageName
  def getDungeonEffects: List[TurnEffect] = Nil
  def getBattleEffects: List[TurnEffect] = Nil
  def getVillageEffects: List[TurnEffect] = Nil
  def attributes: Attributes = Map("Light" -> getLight)
  def getTraits: List[String] = Nil
  def write(connection: Connection): Int = {
    val statement = connection.createStatement()
    statement.execute(s"INSERT INTO Card (name, image, frequency) VALUES ('$name', '$imageName', $frequency)")
    statement.close()
    val queryStatement = connection.createStatement()
    val cardsResultSet = queryStatement.executeQuery(s"SELECT card_id FROM Card where name = '$name'")
    cardsResultSet.next()
    val id = cardsResultSet.getInt(1)
    cardsResultSet.close()
    queryStatement.close()
    id
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
          case "CardBack" => JsSuccess(CardBack)
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
      case CardBack => JsObject(
        Seq(
          "cardType" -> JsString("CardBack"),
          "data" -> JsObject(Seq(
            "id" -> JsNumber(0),
            "name" -> JsString("CardBack"),
           "imageName" -> JsString(CardBack.getImageName),
           "frequency" -> JsNumber(0)
          ))
        )
      )
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
        TurnEffect.parse("Dungeon", attributes("de")))
    } else {
      new ThunderstoneCard(0, name, image, attributes("vp").toInt)
    }
  }

  def apply(info: CardInfo): Card = {
    if (info.heroRow.nonEmpty) {
      HeroCard(info.cardRow, info.heroRow.get, info.turnEffects)
    } else if (info.itemRow.nonEmpty) {
      ItemCard(info.cardRow, info.itemRow.get, info.turnEffects)
    } else if (info.monsterRow.nonEmpty) {
      MonsterCard(
        info.cardRow,
        info.monsterRow.get,
        info.turnEffects,
        info.breachEffects
      )
    } else if (info.spellRow.nonEmpty) {
      SpellCard(info.cardRow, info.spellRow.get, info.turnEffects)
    } else if (info.weaponRow.nonEmpty) {
      WeaponCard(info.cardRow, info.weaponRow.get, info.turnEffects)
    } else if (info.villagerRow.nonEmpty) {
      VillagerCard(info.cardRow, info.villagerRow.get, info.turnEffects)
    } else if (info.cardRow.name == "Disease") {
      DiseaseCard(info.cardRow, info.turnEffects)
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

case object CardBack extends Card("Back", "card000.png", 0)

case class DiseaseCard(
                         id: Int,
                         name: String,
                         imageName: String,
                         dungeonEffects: List[TurnEffect],
                       ) extends Card(name, imageName, 45) {

  override def getDungeonEffects: List[TurnEffect] = dungeonEffects

  override def write(connection: Connection): Int = {
    val id = super.write(connection)
    dungeonEffects.foreach(d => d.write(connection, id))
    id
  }
}

object DiseaseCard {
  implicit val diseaseFormat: Format[DiseaseCard] = Json.format[DiseaseCard]
  def apply(cardRow: Tables.CardRow, turnEffects: Seq[Tables.TurnEffectRow]): DiseaseCard = {
    new DiseaseCard(cardRow.cardId, cardRow.name, cardRow.image,
      turnEffects.map(TurnEffect(_)).toList)
  }
  def apply(name: String, image: String, attributes: Map[String, String]): DiseaseCard = {
    new DiseaseCard(0, name, image,
      TurnEffect.parse("Dungeon", attributes.getOrElse("de", "")))
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
                     battleEffects: List[TurnEffect],
                     dungeonEffects: List[TurnEffect],
                     goldValue: Option[Int],
                     victoryPoints: Int,
                     override val frequency: Int
                   ) extends Card(name, imageName, frequency) {

  override def attributes: Map[String, Int] = super.attributes + ("Strength" -> strength)

  override def getLight: Int = light

  override def getDungeonEffects: List[TurnEffect] = dungeonEffects

  override def getBattleEffects: List[TurnEffect] = battleEffects

  override def getGoldValue: Int = goldValue.getOrElse(0)

  override def hasGoldValue: Boolean = goldValue.nonEmpty

  override def getCost: Option[Int] = Some(cost)

  override def getTraits: List[String] = traits

  def upgradeCost: Option[Int] = level match {
    case 0 => Some(3)
    case 1 => Some(2)
    case 2 =>  Some(3)
    case _ =>  None
  }

  override def write(connection: Connection): Int = {
    val id = super.write(connection)
    val statement = connection.prepareStatement(
      """
        |INSERT INTO Hero (card_id, light, strength, level, cost, gold_value, victory_points, hero_type, hero_classes)
        |     VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        |""".stripMargin)
    statement.setInt(1, id)
    statement.setInt(2, light)
    statement.setInt(3, strength)
    statement.setInt(4, level)
    statement.setInt(5, cost)
    if (goldValue.nonEmpty) {
      statement.setInt(6, goldValue.get)
    } else {
      statement.setNull(6, sql.Types.INTEGER)
    }
    statement.setInt(7, victoryPoints)
    statement.setString(8, heroType)
    statement.setString(9, traits.mkString(","))
    statement.execute()
    battleEffects.foreach(b => b.write(connection, id))
    dungeonEffects.foreach(d => d.write(connection, id))
    id
  }
}

object HeroCard {
  implicit val heroFormat: Format[HeroCard] = Json.format[HeroCard]

  def apply(cardRow: Tables.CardRow, heroRow: Tables.HeroRow, turnEffects: Seq[Tables.TurnEffectRow]): HeroCard = {
    val allEffects = turnEffects.map(TurnEffect(_)).toList
    new HeroCard(cardRow.cardId, cardRow.name, cardRow.image, heroRow.light, heroRow.level, heroRow.strength, heroRow.cost,
      heroRow.heroClasses.split(",").toList, heroRow.heroType, allEffects.filter(_.effectType == "Battle"),
      allEffects.filter(_.effectType == "Dungeon"), heroRow.goldValue, heroRow.victoryPoints, cardRow.frequency)
  }
  def apply(name: String, image: String, attributes: Map[String, String]): HeroCard = {
    new HeroCard(0, name, image, attributes.getOrElse("li", "0").toInt, attributes.getOrElse("lv", "0").toInt,
      attributes("str").toInt, attributes("c").toInt,
      attributes.getOrElse("t", "").split(",").toList.filterNot(_ == ""),
      name.split(" ")(0),
      TurnEffect.parse("Battle", attributes.getOrElse("be", "")),
      TurnEffect.parse("Dungeon", attributes.getOrElse("de", "")),
      attributes.get("gv").map(_.toInt), attributes.getOrElse("vp", "0").toInt, attributes("f").toInt)
  }
}

case class ItemCard(
                     id: Int,
                     name: String,
                     imageName: String,
                     light: Int,
                     cost: Int,
                     traits: List[String],
                     dungeonEffects: List[TurnEffect],
                     goldValue: Int,
                     victoryPoints: Int,
                     override val frequency: Int
                   ) extends Card(name, imageName, frequency) {
  override def getDungeonEffects: List[TurnEffect] = dungeonEffects

  override def getGoldValue: Int = goldValue

  override def getLight: Int = light

  override def hasGoldValue: Boolean = true

  override def getCost: Option[Int] = Some(cost)

  override def getTraits: List[String] = traits

  override def write(connection: Connection): Int = {
    val id = super.write(connection)
    val statement = connection.prepareStatement(
      """
        |INSERT INTO Item (card_id, light, cost, gold_value, victory_points, item_traits)
        |     VALUES (?, ?, ?, ?, ?, ?)
        |""".stripMargin)
    statement.setInt(1, id)
    statement.setInt(2, light)
    statement.setInt(3, cost)
    statement.setInt(4, goldValue)
    statement.setInt(5, victoryPoints)
    statement.setString(6, traits.mkString(","))
    statement.execute()
    dungeonEffects.foreach(d => d.write(connection, id))
    id
  }
}

object ItemCard {
  implicit val itemFormat: Format[ItemCard] = Json.format[ItemCard]
  def apply(cardRow: Tables.CardRow, itemRow: Tables.ItemRow, turnEffects: Seq[Tables.TurnEffectRow]): ItemCard = {
    new ItemCard(cardRow.cardId, cardRow.name, cardRow.image, itemRow.light, itemRow.cost,
      itemRow.itemTraits.split(",").toList, turnEffects.map(TurnEffect(_)).toList, itemRow.goldValue,
      itemRow.victoryPoints, cardRow.frequency)
  }
  def apply(name: String, image: String, attributes: Map[String, String]): ItemCard = {
    new ItemCard(0, name, image, attributes.getOrElse("li", "0").toInt, attributes("c").toInt,
      attributes.getOrElse("t", "").split(",").toList.filterNot(_ == ""),
      TurnEffect.parse("Dungeon", attributes.getOrElse("de", "")),
      attributes("gv").toInt, attributes.getOrElse("vp", "0").toInt, attributes.getOrElse("f", "8").toInt)
  }
}

case class SpellCard(
                      id: Int,
                      name: String,
                      imageName: String,
                      light: Int,
                      cost: Int,
                      traits: List[String],
                      dungeonEffects: List[TurnEffect],
                      victoryPoints: Int
                    ) extends Card(name, imageName, 8) {
  override def getDungeonEffects: List[TurnEffect] = dungeonEffects

  override def getCost: Option[Int] = Some(cost)

  override def getLight: Int = light

  override def getTraits: List[String] = traits

  override def write(connection: Connection): Int = {
    val id = super.write(connection)
    val statement = connection.prepareStatement(
      """
        |INSERT INTO Spell (card_id, light, cost, spell_types, victory_points)
        |     VALUES (?, ?, ?, ?, ?)
        |""".stripMargin)
    statement.setInt(1, id)
    statement.setInt(2, light)
    statement.setInt(3, cost)
    statement.setString(4, traits.mkString(","))
    statement.setInt(5, victoryPoints)
    statement.execute()
    dungeonEffects.foreach(d => d.write(connection, id))
    id
  }
}
object SpellCard {
  implicit val spellFormat: Format[SpellCard] = Json.format[SpellCard]
  def apply(cardRow: Tables.CardRow, spellRow: Tables.SpellRow, turnEffects: Seq[Tables.TurnEffectRow]): SpellCard = {
    new SpellCard(cardRow.cardId, cardRow.name, cardRow.image, spellRow.light, spellRow.cost,
      spellRow.spellTypes.split(",").toList, turnEffects.map(TurnEffect(_)).toList, spellRow.victoryPoints)
  }
  def apply(name: String, image: String, attributes: Map[String, String]): SpellCard = {
    new SpellCard(0, name, image, attributes.getOrElse("li", "0").toInt, attributes("c").toInt,
      attributes.getOrElse("t", "").split(",").toList.filterNot(_ == ""),
      TurnEffect.parse("Dungeon", attributes.getOrElse("de", "")),
      attributes.getOrElse("vp", "0").toInt)
  }
}

case class VillagerCard(
                         id: Int,
                         name: String,
                         imageName: String,
                         cost: Int,
                         traits: List[String],
                         villageEffects: List[TurnEffect],
                         goldValue: Option[Int],
                         victoryPoints: Int
                       ) extends Card(name, imageName, 8) {
  override def getCost: Option[Int] = Some(cost)

  override def getGoldValue: Int = goldValue.getOrElse(0)

  override def hasGoldValue: Boolean = goldValue.nonEmpty

  override def getVillageEffects: List[TurnEffect] = villageEffects

  override def getTraits: List[String] = traits

  override def write(connection: Connection): Int = {
    val id = super.write(connection)
    val statement = connection.prepareStatement(
      """
        |INSERT INTO Villager (card_id, cost, villager_types, gold_value, victory_points)
        |     VALUES (?, ?, ?, ?, ?)
        |""".stripMargin)
    statement.setInt(1, id)
    statement.setInt(2, cost)
    statement.setString(3, traits.mkString(","))
    if (goldValue.nonEmpty) {
      statement.setInt(4, goldValue.get)
    } else {
      statement.setNull(4, sql.Types.INTEGER)
    }
    statement.setInt(5, victoryPoints)
    statement.execute()
    villageEffects.foreach(v => v.write(connection, id))
    id
  }
}

object VillagerCard {
  implicit val villagerFormat: Format[VillagerCard] = Json.format[VillagerCard]
  def apply(cardRow: Tables.CardRow, villagerRow: Tables.VillagerRow,
            turnEffects: Seq[Tables.TurnEffectRow]): VillagerCard = {
    new VillagerCard(cardRow.cardId, cardRow.name, cardRow.image, villagerRow.cost,
      villagerRow.villagerTypes.split(",").toList,
      turnEffects.map(TurnEffect(_)).toList, villagerRow.goldValue,
      villagerRow.victoryPoints)
  }
  def apply(name: String, image: String, attributes: Map[String, String]): VillagerCard = {
    new VillagerCard(0, name, image, attributes("c").toInt,
      attributes.getOrElse("t", "").split(",").toList.filterNot(_ == ""),
      TurnEffect.parse("Village", attributes.getOrElse("ve", "")),
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
                       dungeonEffects: List[TurnEffect],
                       goldValue: Int,
                       victoryPoints: Int,
                       override val frequency: Int
                    ) extends Card(name, imageName, frequency) {
  override def attributes: Map[String, Int] = super.attributes + ("Weight" -> weight, "Equipped" -> 1)

  override def getLight: Int = light

  override def getDungeonEffects: List[TurnEffect] = dungeonEffects

  override def getCost: Option[Int] = Some(cost)

  override def getGoldValue: Int = goldValue

  override def getTraits: List[String] = traits

  override def hasGoldValue: Boolean = true

  override def write(connection: Connection): Int = {
    val id = super.write(connection)
    val statement = connection.prepareStatement(
      """
        |INSERT INTO Weapon (card_id, light, weight, cost, weapon_types, gold_value, victory_points)
        |     VALUES (?, ?, ?, ?, ?, ?, ?)
        |""".stripMargin)
    statement.setInt(1, id)
    statement.setInt(2, light)
    statement.setInt(3, weight)
    statement.setInt(4, cost)
    statement.setString(5, traits.mkString(","))
    statement.setInt(6, goldValue)
    statement.setInt(7, victoryPoints)
    statement.execute()
    dungeonEffects.foreach(d => d.write(connection, id))
    id
  }
}

object WeaponCard {
  implicit val weaponFormat: Format[WeaponCard] = Json.format[WeaponCard]
  def apply(
             cardRow: Tables.CardRow, weaponRow: Tables.WeaponRow, turnEffects: Seq[Tables.TurnEffectRow]
           ): WeaponCard = {
    new WeaponCard(cardRow.cardId, cardRow.name, cardRow.image, weaponRow.light, weaponRow.weight, weaponRow.cost,
      weaponRow.weaponTypes.split(",").toList, turnEffects.map(TurnEffect(_)).toList, weaponRow.goldValue,
      weaponRow.victoryPoints, cardRow.frequency)
  }
  def apply(name: String, image: String, attributes: Map[String, String]): WeaponCard = {
    new WeaponCard(0, name, image, attributes.getOrElse("li", "0").toInt, attributes.getOrElse("w", "0").toInt,
      attributes("c").toInt, attributes.getOrElse("t", "").split(",").toList.filterNot(_ == ""),
      TurnEffect.parse("Dungeon", attributes.getOrElse("de", "")),
      attributes.getOrElse("gv", "0").toInt, attributes.getOrElse("vp", "0").toInt,
      attributes.getOrElse("f", "8").toInt)
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
                        battleEffects: List[TurnEffect],
                        dungeonEffects: List[TurnEffect],
                        goldValue: Int,
                        victoryPoints: Int,
                        experiencePoints: Int,
                        override val frequency: Int
                      ) extends Card(name, imageName, frequency) {
  override def getDungeonEffects: List[TurnEffect] = dungeonEffects

  override def getLight: Int = light

  override def getBattleEffects: List[TurnEffect] = battleEffects

  override def getGoldValue: Int = goldValue

  override def hasGoldValue: Boolean = true

  override def getTraits: List[String] = "Monster" :: traits

  def monsterType: String = {
    if (traits.head == "Dragon") {
      "Dragon"
    } else {
      traits.mkString(" ")
    }
  }
  override def write(connection: Connection): Int = {
    val id = super.write(connection)
    val statement = connection.prepareStatement(
      """
        |INSERT INTO Monster (card_id, light, health, monster_types, gold_value, victory_points, experience)
        |     VALUES (?, ?, ?, ?, ?, ?, ?)
        |""".stripMargin)
    statement.setInt(1, id)
    statement.setInt(2, light)
    statement.setInt(3, health)
    statement.setString(4, traits.mkString(","))
    statement.setInt(5, goldValue)
    statement.setInt(6, victoryPoints)
    statement.setInt(7, experiencePoints)
    statement.execute()
    dungeonEffects.foreach(_.write(connection, id))
    battleEffects.foreach(_.write(connection, id))
    breachEffect.foreach(_.write(connection, id))
    id
  }
}

object MonsterCard {
  implicit val monsterFormat: Format[MonsterCard] = Json.format[MonsterCard]
  def apply(cardRow: Tables.CardRow, monsterRow: Tables.MonsterRow, turnEffects: Seq[Tables.TurnEffectRow],
            breachEffects: Seq[Tables.BreachEffectRow]): MonsterCard = {
    val breachEffect: Option[BreachEffect] = getBreachEffect(breachEffects.headOption.map(_.effect))
    val clashEffects: List[TurnEffect] = turnEffects.map(TurnEffect(_)).toList
    new MonsterCard(cardRow.cardId, cardRow.name, cardRow.image, monsterRow.light, monsterRow.health,
      monsterRow.monsterTypes.split(",").toList, breachEffect,
      clashEffects.filter(_.effectType == "Battle"), clashEffects.filter(_.effectType == "Dungeon"),
      monsterRow.goldValue, monsterRow.victoryPoints,
      monsterRow.experience, cardRow.frequency)
  }

  private def getBreachEffect(breachEffects: Option[String]): Option[BreachEffect] = {
    val breachEffect = breachEffects match {
      case Some(effect) if effect == "ReduceHeroes" =>
        Some(DestroyTwoHeroesFromVillagePiles)
      case Some(effect) if effect == "DestroyTwoHeroesFromVillagePiles" =>
        Some(DestroyTwoHeroesFromVillagePiles)
      case Some(effect) if effect == "Discard2" =>
        Some(DiscardTwoCards)
      case Some(effect) if effect == "DiscardTwoCards" =>
        Some(DiscardTwoCards)
      case None => None
    }
    breachEffect
  }

  def apply(name: String, image: String, attributes: Map[String, String]): MonsterCard = {
    new MonsterCard(0, name, image, attributes.getOrElse("li", "0").toInt, attributes("health").toInt,
      attributes.getOrElse("t", "").split(",").toList.filterNot(_ == ""),
      getBreachEffect(attributes.get("breach")),
      TurnEffect.parse("Battle", attributes.getOrElse("be", "")),
      TurnEffect.parse("Dungeon", attributes.getOrElse("de", "")),
      attributes("gv").toInt, attributes("vp").toInt, attributes("xp").toInt, attributes.getOrElse("f", "2").toInt)
  }
}

case class ThunderstoneCard( id: Int, name: String, imageName: String, victoryPoints: Int)
  extends Card(name, imageName, 1) {
  override def write(connection: Connection): Int = {
    val id = super.write(connection)
    val statement = connection.prepareStatement(
      """
        |INSERT INTO Thunderstone (card_id, victory_points)
        |     VALUES (?, ?)
        |""".stripMargin)
    statement.setInt(1, id)
    statement.setInt(2, victoryPoints)
    statement.execute()
    id
  }

}
object ThunderstoneCard {
  implicit val thunderstoneFormat: Format[ThunderstoneCard] = Json.format[ThunderstoneCard]
  def apply(row: Tables.CardRow): ThunderstoneCard = {
    new ThunderstoneCard(row.cardId, row.name, row.image, 3)
  }
}



