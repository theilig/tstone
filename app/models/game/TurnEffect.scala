package models.game

import java.sql.{Connection, Types}

import models.schema.Tables
import play.api.libs.json.{Format, Json}
import services.CardManager

case class TurnEffect(
                       effectType: String,
                       effect: Option[String],
                       requiredType: Option[String],
                       repeatable: Boolean,
                       adjustment: Option[AttributeAdjustment]
                     ) {
  private def combinedOperation(adjustment: AttributeAdjustment): Boolean = {
    adjustment.operation match {
      case Divide => true
      case Multiply => true
      case _ => false
    }
  }

  def isCombined: Boolean = {
    (adjustment.exists(combinedOperation) && !repeatable) || requiredType.contains("LightPenalty")
  }

  def isGeneralEffect: Boolean = {
    repeatable && effectType != "Destroy"
  }
  def isIndividualCardEffect: Boolean = {
    !isCombined && !isGeneralEffect
  }

  def write(connection: Connection, id: Int): Unit = {
    val statement = connection.prepareStatement(
      """
        |INSERT INTO TurnEffect (card_id, effect_type, effect, need_type, repeatable, operation, modifier_amount, attribute_modified)
        |     VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        |""".stripMargin)
    statement.setInt(1, id)
    statement.setString(2, effectType)
    if (effect.nonEmpty) {
      statement.setString(3, effect.get)
    } else {
      statement.setNull(3, Types.VARCHAR)
    }
    if (requiredType.nonEmpty) {
      statement.setString(4, requiredType.get)
    } else {
      statement.setNull(4, Types.VARCHAR)
    }
    statement.setBoolean(5, repeatable)
    if (adjustment.nonEmpty) {
      statement.setString(6, adjustment.get.operation.getString)
      statement.setInt(7, adjustment.get.amount)
      statement.setString(8, adjustment.get.attribute)
    } else {
      statement.setNull(6, Types.VARCHAR)
      statement.setNull(7, Types.INTEGER)
      statement.setNull(8, Types.VARCHAR)
    }
    statement.execute()
  }

  def isActive(targetCard: Card, destroyed: Option[List[Card]]): Boolean = {
    (effect, requiredType) match {
      case (_, None) => true
      case (Some("Destroy"), Some("Self")) => destroyed.get.exists(_.getName == targetCard.getName)
      case (Some("Destroy"), Some(cardType)) if destroyed.nonEmpty => destroyed.get.exists(_.getName == cardType)
    }
  }

  def isCombinedActive(attributes: Map[String, Int]): Boolean =
    requiredType match {
      case None => true
      case Some("LightPenalty") => attributes.getOrElse("Light", 0) < 0
      case Some(missing) if missing.startsWith("!") => attributes.getOrElse(missing.substring(1), 0) == 0
      case _ => false
    }

  def isDestroy: Boolean = effect.contains("Destroy")

  def spoils: Option[String] = effectType match {
    case "Spoils" => effect
    case _ => None
  }

  def adjustAttributes(currentValues: Attributes, oldCard: Option[Card] = None): Attributes = {
    adjustment.map {
      case AttributeAdjustment(op, amount, attribute) =>
        (op, attribute) match {
          case (Net, "Gold") => currentValues + ("Gold" ->
            (currentValues.getOrElse("Gold", 0) + amount - oldCard.get.getGoldValue))
          case (Add, a) => currentValues + (a -> (currentValues.getOrElse(a, 0) + amount))
          case (Subtract, a) => currentValues + (a -> (currentValues.getOrElse(a, 0) - amount))
          // Don't multiply a -1 by 0 (i.e. disease effect)
          case (Multiply, a) if amount == 0 && currentValues.get(a).exists(_ < 0) => currentValues
          case (Multiply, a)  => currentValues + (a -> (currentValues.getOrElse(a, 0) * amount))
          case (Divide, a) => currentValues + (a -> (currentValues.getOrElse(a, 0) / amount))
        }
    }.getOrElse(currentValues)
  }

  def matchesRequiredCard(card: Card, isSelf: Boolean = false): Boolean = {
    requiredType.forall(required => {
      (required, card) match {
        case ("Self", _) => isSelf
        case _ => CardManager.matchesType(card, required)
      }
    })
  }

  def applyIndividualAdjustment(card: Card, destroyed: Boolean)(attributes: Attributes): Attributes = {
    val matchesCard = adjustment.filterNot(a => {
      isCombined || List("Light", "Card").contains(a.attribute)
    }).map(_ => {
      (requiredType, card) match {
        case (None, _) => true
        case (Some("Weapon"), _: WeaponCard) => true
        case (Some("Hero"), _: HeroCard) => true
        case (Some("!Fighter"), h: HeroCard) => !h.traits.contains("Fighter")
        case (Some("Food"), i: ItemCard) => i.traits.contains("Foot")
        case (Some("Item+Light"), i: ItemCard) => i.getLight > 0
        case (Some("Item"), _: ItemCard) => true
        case (Some("Monster"), _: MonsterCard) => true
        case (Some("MASpell"), s: SpellCard) =>
          s.dungeonEffects.exists(p => p.adjustment.exists(a => a.attribute == "Magic Attack"))
        case (Some("Militia"), h: HeroCard) => h.getName == "Militia"
        case (Some("Weapon+Edged"), w: WeaponCard) => w.traits.contains("Edged")
        case (Some("Self"), _) if effect.contains("Destroy") => destroyed
        case _ => false
      }
    })
    if (matchesCard.getOrElse(false)) {
      adjustAttributes(attributes)
    } else {
      attributes
    }
  }
  def applyMatchupAdjustment(
                              slot: BattleSlot,
                              monster: MonsterCard,
                              rank: Int,
                              late: Boolean)(attributes: Attributes): Attributes = {
    def equipped(p: Card => Boolean): Boolean = {
      (slot.equippedCards ::: slot.destroyedCards).collect({
        case w: WeaponCard => p(w)
      }).nonEmpty
    }

    val matchesCard = adjustment.filterNot(a => {
      isCombined || List("Card").contains(a.attribute)
    }).exists(_ => {
      requiredType.map {
        case "!Magic Attack" => attributes.getOrElse("Magic Attack", 0) <= 0
        case "!Equipped" => !equipped(_ => true)
        case "Equipped" => equipped(_ => true)
        case "!Rank1" => rank > 1
        case "Rank3" => rank >= 3
        case "Equipped+Edged" => equipped(c => c.getTraits.contains("Edged"))
        case "ClericVDoomknight" =>
          slot.baseCard.getTraits.contains("Cleric") && monster.getTraits.contains("Doomknight")
        case "ClericVUndead" =>
          slot.baseCard.getTraits.contains("Cleric") && monster.getTraits.contains("Undead")
        case "Hero8Strength" => slot.baseCard match {
          case _: HeroCard => attributes.getOrElse("Strength", 0) >= 8
          case _ => false
        }
      }.getOrElse(late)
    })
    if (matchesCard) {
      if (isCombined && late) {
        adjustAttributes(attributes)
      } else if (!isCombined && !late) {
        adjustAttributes(attributes)
      } else {
        attributes
      }
    } else {
      attributes
    }
  }
}

object TurnEffect {
  implicit val turnEffectFormat: Format[TurnEffect] = Json.format[TurnEffect]
  def apply(row: Tables.TurnEffectRow): TurnEffect = {
    val adjustment = row.operation.map(op =>
      AttributeAdjustment(Operation(op), row.modifierAmount.get, row.attributeModified.get)
    )
    new TurnEffect(row.effectType, row.effect, row.needType, row.repeatable, adjustment)
  }
  def parse(effectType: String, data: String): List[TurnEffect] = {
    val effects = data.split(",")
    if (data.nonEmpty) {
      effects.map(effect => {
        val fields = effect.split(";")
        val description = Option(fields(0)).collect { case x if x.trim.nonEmpty => x }
        val required = Option(fields(1)).collect { case x if x.trim.nonEmpty => x }
        val repeated = fields(2) match {
          case "1" => true
          case "0" => false
        }
        val adjustment =
          if (fields.length > 3 && fields(3).nonEmpty) {
            Some(AttributeAdjustment(Operation(fields(3)), fields(4).toInt, fields(5)))
          } else {
            if (fields.length > 3 && (fields(4).nonEmpty || fields(5).nonEmpty)) {
              throw new IllegalArgumentException()
            }
            None
          }
        new TurnEffect(effectType, description, required, repeated, adjustment)
      }).toList
    } else {
      Nil
    }
  }
}
