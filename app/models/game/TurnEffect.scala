package models.game

import java.sql.{Connection, Types}

import models.schema.Tables
import play.api.libs.json.{Format, Json}

case class TurnEffect(
                       effectType: String,
                       effect: Option[String],
                       requiredType: Option[String],
                       repeatable: Boolean,
                       adjustment: Option[AttributeAdjustment]
                     ) {
  def activate(state: State): State = state
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
