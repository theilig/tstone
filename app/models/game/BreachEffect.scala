package models.game

import play.api.libs.json.{Format, JsError, JsObject, JsPath, JsString, JsSuccess, Reads, Writes}

trait BreachEffect

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

case object DestroyTwoHeroesFromVillagePiles extends BreachEffect

case object DiscardTwoCards extends BreachEffect
