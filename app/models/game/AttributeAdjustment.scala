package models.game

import play.api.libs.json.{Format, JsError, JsString, JsSuccess, Json, Reads, Writes}

sealed trait Operation {
  def getString: String
}
case object Add extends Operation {
  override def getString: String = "ADD"
}
case object Subtract extends Operation {
  override def getString: String = "SUBTRACT"
}
case object Multiply extends Operation {
  override def getString: String = "MULTIPLY"
}
case object Divide extends Operation {
  override def getString: String = "DIVIDE"
}
case object Net extends Operation {
  override def getString: String = "NET"
}

object Operation {
  def apply(text: String): Operation = {
    text match {
      case "ADD" => Add
      case "NET" => Net
      case "SUBTRACT" => Subtract
      case "MULTIPLY" => Multiply
      case "DIVIDE" => Divide
    }
  }
  implicit val operationFormat: Format[Operation] = Format[Operation](
    Reads {
      case JsString(s) if s == "Add" => JsSuccess(Add)
      case JsString(s) if s == "Net" => JsSuccess(Net)
      case JsString(s) if s == "Subtract" => JsSuccess(Subtract)
      case JsString(s) if s == "Multiply" => JsSuccess(Multiply)
      case JsString(s) if s == "Divide" => JsSuccess(Divide)
      case _ => JsError("operationType undefined or incorrect")
    },
    Writes {
      case Add => JsString("Add")
      case Net => JsString("Net")
      case Subtract => JsString("Subtract")
      case Multiply => JsString("Multiply")
      case Divide => JsString("Divide")
    }
  )
}

case class AttributeAdjustment(operation: Operation, amount: Int, attribute: String)

object AttributeAdjustment {
  implicit val attributeAdjustmentFormat: Format[AttributeAdjustment] = Json.format[AttributeAdjustment]
}