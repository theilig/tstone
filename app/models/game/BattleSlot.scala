package models.game

import play.api.libs.json.{Format, Json}

case class BattleSlot(card: String, equipped: List[String], destroyed: List[String], hand: Option[List[Card]]) {
  def battleAttributes(generalEffects: List[TurnEffect], monster: Option[MonsterCard], rank: Int): (String, Attributes) = {
    baseCard match {
      case w: WeaponCard => w.name -> Map()
      case _ =>
        val monsterEffects = monster.map(_.getBattleEffects).getOrElse(Nil)
        val allCards = (baseCard :: equippedCards).map(c => (c, false)) :::
          destroyedCards.filter(_.getDungeonEffects.exists(
            e => e.requiredType.contains("Self"))
          ).map(c => (c, true))
        val slotAttributes = allCards.map(pair => {
          val (card, destroyed) = pair
          applyAdjustments(
            card.attributes,
            ( monsterEffects ::: generalEffects ::: card.getDungeonEffects).map(e => e.applyIndividualAdjustment(card, destroyed))
          )
        })
        val slotResult = applyAdjustments(
          combineAttributes(slotAttributes),
          allCards.flatMap(_._1.getDungeonEffects).map(e => e.applyMatchupAdjustment(this, monster, rank, late = false))
        )
        baseCard.getName -> applyAdjustments(
          slotResult,
          monsterEffects.map(e => e.applyMatchupAdjustment(this, monster, rank, late = false))
        )
    }
  }

  def villageAttributes: Attributes = {
    val liveCards = baseCard :: equippedCards
    val slotAttributes = liveCards.map(_.attributes) ::: baseCard.getVillageEffects.map(e =>
      e.applyMatchupAdjustment(this, None, 0, late = false)(
        e.applyIndividualAdjustment(baseCard, destroyed.contains(baseCard.getName))(Map()))
    )
    combineAttributes(slotAttributes)
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