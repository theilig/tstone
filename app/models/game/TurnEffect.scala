package models.game

import play.api.libs.json.{Format, Json}
import models.schema.Tables

trait TurnEffect {
  def activate(state: State): State
}

object TurnEffect {
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
        effectType match {
          case "Battle" => BattleEffect(description, required, repeated, adjustment)
          case "Dungeon" => DungeonEffect(description, required, repeated, adjustment)
          case "Village" => VillageEffect(description, required, repeated, adjustment)
        }
      }).toList
    } else {
      Nil
    }
  }
}

case class VillageEffect(
                     effect: Option[String],
                     requiredType: Option[String],
                     repeatable: Boolean,
                     adjustment: Option[AttributeAdjustment]
                   ) extends TurnEffect {
  override def activate(state: State): State = state
}

object VillageEffect {
  implicit val villageEffectFormat: Format[VillageEffect] = Json.format[VillageEffect]
  def apply(row: Tables.VillageEffectRow): VillageEffect = {
    val adjustment = row.operation.map(op =>
      AttributeAdjustment(Operation(op), row.modifierAmount.get, row.attributeModified.get)
    )
    new VillageEffect(row.effect, row.needType, row.repeatable, adjustment)
  }
}

case class BattleEffect (
                     effect: Option[String],
                     requiredType: Option[String],
                     repeatable: Boolean,
                     adjustment: Option[AttributeAdjustment]
                   ) extends TurnEffect {
  override def activate(state: State): State = state
}

object BattleEffect {
  implicit val battleEffectFormat: Format[BattleEffect] = Json.format[BattleEffect]
  def apply(row: Tables.BattleEffectRow): BattleEffect = {
    val adjustment = row.operation.map(op =>
      AttributeAdjustment(Operation(op), row.modifierAmount.get, row.attributeModified.get)
    )
    new BattleEffect(row.effect, row.needType, row.repeatable, adjustment)
  }
}

case class DungeonEffect (
                      effect: Option[String],
                      requiredType: Option[String],
                      repeatable: Boolean,
                      adjustment: Option[AttributeAdjustment]
                    ) extends TurnEffect {
  override def activate(state: State): State = state
}
object DungeonEffect {
  implicit val dungeonEffectFormat: Format[DungeonEffect] = Json.format[DungeonEffect]
  def apply(row: Tables.DungeonEffectRow): DungeonEffect = {
    val adjustment = row.operation.map(op =>
      AttributeAdjustment(Operation(op), row.modifierAmount.get, row.attributeModified.get)
    )
    new DungeonEffect(row.effect, row.needType, row.repeatable, adjustment)
  }
}

