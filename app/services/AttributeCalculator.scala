package services

import models.game.{AttributeResult, Attributes, BattleSlot, Card, MonsterCard, Player, combineAttributes}

object AttributeCalculator {
  def getValues(
                 player: Player, slots: List[BattleSlot], possibleBattle: Option[(Int, MonsterCard)]
               ): AttributeResult = {

    val effectsHand = CardManager.removeDestroyed(player.hand, slots, keepSelfDestroyed = true)
    val possibleMonster = possibleBattle.map(_._2)
    val rank = possibleBattle.map(_._1 + 1).getOrElse(0)
    val handGeneralEffects = effectsHand.flatMap(
      _.getDungeonEffects.filter(_.isGeneralEffect)
    )
    val generalEffects = possibleMonster.map(m =>
      m.battleEffects.filter(_.isGeneralEffect)
    ).getOrElse(Nil) ::: handGeneralEffects

    val battleAttributes = slots.map(s =>
      s.copy(hand = Some(player.hand)).battleAttributes(generalEffects, possibleMonster, rank)._2)
    val finalBattleAttributes = finalAttributes(effectsHand, possibleMonster, rank, battleAttributes)

    val villageAttributes = combineAttributes(slots.map(s =>
      s.copy(hand = Some(player.hand)).villageAttributes))
    AttributeResult(combineAttributes(List(
      villageAttributes,
      finalBattleAttributes,
      Map("Buys" -> 1, "Experience" -> player.xp)))
    )
  }

  def finalAttributes(
                       finalHand: List[Card],
                       monster: Option[MonsterCard],
                       rank: Int,
                       attributes: Seq[Attributes]): Attributes = {
    val combinedAttributes = combineAttributes(attributes.filter(!_.contains("No Attack")))
    val lightAdjustedAttributes = combinedAttributes + ("Light" -> (combinedAttributes.getOrElse("Light", 0) - rank))
    val combinedEffects = (finalHand.flatten(c => c.getDungeonEffects) :::
      monster.map(_.battleEffects).getOrElse(Nil)).filter(_.isCombined)
    val adjustedAttributes = combinedEffects.foldLeft(lightAdjustedAttributes)((a, e) => {
      if (e.isCombinedActive(a)) {
        e.adjustAttributes(a)
      } else {
        a
      }
    })
    adjustedAttributes
  }
}
