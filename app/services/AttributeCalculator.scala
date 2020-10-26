package services

import models.game.{AttributeResult, BattleSlot, GameError, MonsterCard, Player, combineAttributes}

object AttributeCalculator {
  def getValues(
                 player: Player, slots: List[BattleSlot], monster: Option[(Int, MonsterCard)]
               ): Either[GameError, AttributeResult] = {
    Right(AttributeResult(combineAttributes(slots.map(s =>
      s.copy(hand = Some(player.hand)).battleAttributes(Nil, monster.map(_._2), monster.map(_._1).getOrElse(0))._2))))
  }
}
