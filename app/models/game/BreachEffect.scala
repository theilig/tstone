package models.game

import java.sql.Connection

import play.api.libs.json.{Format, JsError, JsObject, JsPath, JsString, JsSuccess, Reads, Writes}

trait BreachEffect {
  def getString: String
  def write(connection: Connection, id: Int): Unit = {
    val statement = connection.prepareStatement(
      """
        |INSERT INTO BreachEffect (card_id, effect)
        |     VALUES (?, ?)
        |""".stripMargin)
    statement.setInt(1, id)
    statement.setString(2, getString)
    statement.execute()
    statement.close()
  }
}

object BreachEffect {
  implicit val breachFormat: Format[BreachEffect] = Format[BreachEffect](
   Reads {js =>
     val effect = (JsPath \ "effect").read[String].reads(js)
     effect.fold(
       _ => JsError("effect undefined or incorrect"), {
         case "DestroyTwoHeroesFromVillagePiles" =>
           JsSuccess(DestroyTwoHeroesFromVillagePiles)
         case "DiscardTwoCards"  =>
           JsSuccess(DiscardTwoCards)
       }
     )
   },
    Writes (breach =>
      JsObject(Seq("effect" -> (breach match {
        case DiscardTwoCards => JsString("DiscardTwoCards")
        case DestroyTwoHeroesFromVillagePiles => JsString("DestroyTwoHeroesFromVillagePiles")
      })))
    )
  )
}

case object DestroyTwoHeroesFromVillagePiles extends BreachEffect {
  override def getString: String = "DestroyTwoHeroesFromVillagePiles"
}

case object DiscardTwoCards extends BreachEffect {
  override def getString: String = "DiscardTwoCards"
}
