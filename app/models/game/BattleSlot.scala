package models.game

import play.api.libs.json.{Format, Json}

case class BattleSlot(card: String, equipped: List[String], destroyed: List[String]) {
  def baseCard(hand: List[Card]): Card = {
        hand.find(_.getName == card).get
  }
  def equippedCards(hand: List[Card]): List[Card] = {
    equipped.map(c => hand.find(_.getName == c).get)
  }
  def destroyedCards(hand: List[Card]): List[Card] = {
    destroyed.map(c => hand.find(_.getName == c).get)
  }
}

object BattleSlot {
  implicit val battleSlotFormat: Format[BattleSlot] = Json.format
}