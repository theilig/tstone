package models.game

import play.api.libs.json.{Format, Json}

case class BattleSlot(card: String, equipped: List[String], destroyed: List[String], hand: Option[List[Card]]) {
  def battleAttributes(generalEffects: List[TurnEffect], monster: MonsterCard, rank: Int): Map[String, Int] = {
    val allCards = (baseCard :: equippedCards).map(c => (c, false)) :::
      destroyedCards.filter(_.getDungeonEffects.exists(
        e => e.requiredType.contains("Self"))
      ).map(c => (c, true))
    val slotAttributes = allCards.map(pair => {
        val (card, destroyed) = pair
        applyAdjustments(
          card.attributes,
          (monster.getBattleEffects ::: generalEffects).map(e => e.applyIndividualAdjustment(card, destroyed))
        )
    })
    val slotResult = applyAdjustments(
      combineAttributes(slotAttributes),
      allCards.flatMap(_._1.getDungeonEffects).map(e => e.applyMatchupAdjustment(this, monster, rank, late = false))
    )
    val lateResult = applyAdjustments(
      slotResult,
      allCards.flatMap(_._1.getDungeonEffects).map(e => e.applyMatchupAdjustment(this, monster, rank, late = true))
    )
    applyAdjustments(
      lateResult,
      monster.getBattleEffects.map(e => e.applyMatchupAdjustment(this, monster, rank, late = true))
    )
  }

  def baseCard: Card = {
      hand.get.find(_.getName == card).get
  }
  def equippedCards: List[Card] = {
    equipped.map(c => hand.get.find(_.getName == c).get)
  }
  def destroyedCards: List[Card] = {
    destroyed.map(c => hand.get.find(_.getName == c).get)
  }
}

object BattleSlot {
  implicit val battleSlotFormat: Format[BattleSlot] = Json.format
}