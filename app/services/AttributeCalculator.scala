package services

import models.game.{AttributeResult, BattleSlot, MonsterCard, Player, combineAttributes}

object AttributeCalculator {
  def getValues(
                 player: Player, slots: List[BattleSlot], monster: Option[(Int, MonsterCard)]
               ): AttributeResult = {

    val handGeneralEffects = CardManager.removeDestroyed(player.hand, slots, keepSelfDestroyed = true).flatMap(
      _.getDungeonEffects.filter(_.isGeneralEffect)
    )
    val generalEffects = monster.map(m =>
      m._2.battleEffects.filter(_.isGeneralEffect)
    ).getOrElse(Nil) ::: handGeneralEffects

    val battleAttributes = combineAttributes(slots.map(s =>
      s.copy(hand = Some(player.hand)).battleAttributes(generalEffects, monster.map(_._2), monster.map(_._1).getOrElse(0))._2))
    val villageAttributes = combineAttributes(slots.map(s =>
      s.copy(hand = Some(player.hand)).villageAttributes))
    AttributeResult(combineAttributes(List(
      villageAttributes,
      battleAttributes,
      Map("Buys" -> 1, "Experience" -> player.xp)))
    )
  }
}
