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
    adjustment.exists(combinedOperation)
  }
  def isLate: Boolean = {
    isCombined || effectType == "Battle"
  }
  def isMatchupEffect: Boolean = {
    false
  }
  def isEquippedEffect: Boolean = {
    false
  }

  def isGeneralEffect: Boolean = {
    repeatable && requiredType.nonEmpty && effectType != "Destroy"
  }
  def isIndividualCardEffect: Boolean = {
    !isCombined && !isLate && !isMatchupEffect && !isEquippedEffect && !isGeneralEffect
  }

  def individualEffectActive(slot: List[Card], monster: Card): Boolean = {
    requiredType.isEmpty
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

  def isCombinedActive(attributes: Map[String, Int]): Boolean = false

  def isDestroy: Boolean = effectType == "Destroy"

  def spoils: Option[String] = effectType match {
    case "Spoils" => effect
    case _ => None
  }

  def adjustAttributes(currentValues: Map[String, Int], oldCard: Option[Card] = None): Map[String, Int] = {
    adjustment.map {
      case AttributeAdjustment(op, amount, attribute) =>
        (op, attribute) match {
          case (Net, "Gold") => currentValues + ("Gold" ->
            (currentValues.getOrElse("Gold", 0) + amount - oldCard.get.getGoldValue))
          case (Add, a) => currentValues + (a -> (currentValues.getOrElse(a, 0) + amount))
          case (Subtract, a) => currentValues + (a -> (currentValues.getOrElse(a, 0) - amount))
          case (Multiply, a) => currentValues + (a -> (currentValues.getOrElse(a, 0) * amount))
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
