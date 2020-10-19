package controllers.game.stage

import controllers.GameException
import models.User
import models.game._
import play.api.libs.json.{Format, Json}
import services.CardManager

case class Crawling(currentPlayerId: Int) extends PlayerStage {

  def removeDestroyed(hand: List[Card], arrangement: List[BattleSlot], includeSelfDestroyed: Boolean): List[Card] = {
    arrangement match {
      case Nil => hand
      case x :: remaining =>
        removeDestroyed(
          x.destroyed.foldLeft(hand)((newHand, destroyedCard) => {
            if (destroyedCard != x.card || !includeSelfDestroyed)
              CardManager.removeOneInstanceFromCards(newHand, destroyedCard)
            else
              newHand
          }),
          remaining, includeSelfDestroyed
        )
    }
  }

  def addGeneralEffects(
                         generalEffectsHand: List[Card],
                         target: Card,
                         attributes: Map[String, Int]
                       ): Map[String, Int] = {
    generalEffectsHand.foldLeft(attributes)((a, c) => {
      c.getDungeonEffects.filter(_.isGeneralEffect).
        filter(_.matchesRequiredCard(target, target.getName == c.getName)).foldLeft(a)((currentAttributes, effect) => {
        effect.adjustAttributes(currentAttributes, Some(target))
      })
    })
  }

  def defeat(monster: MonsterCard, rank: Int, finalHand: List[Card], state: State): State = {
    val newDungeon = state.dungeon.map(_.defeat(rank))
    val newState = getDiseases(monster, state.updatePlayer(currentPlayerId)(p =>
      p.copy(discard = monster :: p.discard, hand = finalHand, xp = p.xp + monster.experiencePoints)
    ).copy(
      dungeon = newDungeon
    ))
    val possibleDestroys = getPossibleDestroys(monster, finalHand)
    if (possibleDestroys.length > 1) {
      newState.copy(currentStage = Destroying(currentPlayerId, possibleDestroys))
    } else {
      checkSpoils(newState)
    }
  }

  def fightOff(monster: MonsterCard, rank: Int, finalHand: List[Card], state: State): State = {
    val newDungeon = state.dungeon.map(_.fightOff(rank))
    val newState = getDiseases(monster, state.updatePlayer(currentPlayerId)(p =>
      p.copy(hand = finalHand)
    ).copy(
      dungeon = newDungeon
    ))
    val possibleDestroys: List[Card] = getPossibleDestroys(monster, finalHand)
    if (possibleDestroys.length > 1) {
      newState.copy(currentStage = Destroying(currentPlayerId, possibleDestroys))
    } else {
      endTurn(newState)
    }
  }

  private def getPossibleDestroys(monster: MonsterCard, finalHand: List[Card]): List[Card] = {
    val possibleDestroys = monster.battleEffects.filter(_.isDestroy).foldLeft(List[Card]())((soFar, effect) => {
      soFar ::: finalHand.filter(c => effect.matchesRequiredCard(c))
    })
    possibleDestroys
  }

  private def getDiseases(monster: MonsterCard, state: State): State = {
    val diseaseCard = state.village.get.takeCard("Disease")._2.get

    monster.battleEffects.foldLeft(state)((state, e) => {
      if (e.effect.contains("Spoils") && e.requiredType.contains("Disease")) {
        state.updatePlayer(currentPlayerId)(p => p.copy(discard = diseaseCard :: p.discard))
      } else {
        state
      }
    })
  }

  def canCarry(heroAttributes: Map[String, Int], itemAttributes: Map[String, Int]): Boolean = {
    itemAttributes.get("Weight").forall(w => w <= heroAttributes("Strength"))
  }

  def resolveBattle(
                     state: State,
                     finalHand: List[Card],
                     monster: MonsterCard,
                     rank: Int,
                     attributes: Map[String, Int]
                   ): State = {
    val lightAdjustedAttributes = attributes + ("Light" -> (attributes.getOrElse("Light", 0) - rank))
    val adjustedAttributes = monster.battleEffects.filter(_.isCombined).foldLeft(lightAdjustedAttributes)((a, e) => {
      if (e.isCombinedActive(a)) {
        e.adjustAttributes(a)
      } else {
        a
      }
    })
    if (adjustedAttributes.contains("No Attack")) {
      throw new GameException("You can't attack this monster")
    }
    val lightPenalty = -Math.min(0, adjustedAttributes.getOrElse("Light", 0)) * state.dungeon.get.lightPenalty
    if (adjustedAttributes.getOrElse("Attack", 0) + adjustedAttributes.getOrElse("Magic Attack", 0) >=
      monster.health + lightPenalty)
      defeat(monster, rank, finalHand, state)
    else {
      fightOff(monster, rank, finalHand, state)
    }
  }

  def combineAttributes(attributes: List[Map[String, Int]]): Map[String, Int] = {
    attributes.foldLeft(Map[String, Int]())((a, b) => {
      b.foldLeft(a)((a, pair) => {
        val (key, value) = pair
        a + (key -> (value + a.getOrElse(key, 0)))
      })
    })
  }

  def applyIndividualEffects(
                              cards: List[Card],
                              getEffects: Card => List[TurnEffect],
                              monster: MonsterCard,
                              attributes: Map[String, Int]
                            ): Map[String, Int] = {
    cards.foldLeft(attributes)((a, card) => {
      getEffects(card).filter(_.isIndividualCardEffect).filter(
        _.individualEffectActive(cards, monster)).foldLeft(a)((currentAttributes, effect) => {
        effect.adjustAttributes(currentAttributes, None)
      })
    })

  }
  def receive(message: Message, user: User, state: State): Either[GameError, State] =
    message match {
      case Battle(monsterIndex, cardArrangement) =>
        val hand = currentPlayer(state).get.hand
        try {
          val monster = state.dungeon.get.monsterPile.drop(monsterIndex - 1).headOption match {
            case Some(m: MonsterCard) => m
            case _ => throw new GameException("Invalid monster to battle")
          }
          val generalEffectsHand = removeDestroyed(hand, cardArrangement, includeSelfDestroyed = true)
          val finalHand = removeDestroyed(hand, cardArrangement, includeSelfDestroyed = false)
          val attributes = cardArrangement.map(slot => {
            val cards = (finalHand.find(_.getName == slot.card) ::
              slot.equipped.map(name => finalHand.find(_.getName == name))).flatten
            val cardAttributes = cards.head match {
              case _: WeaponCard => List(Map[String, Int]()) // Unequipped weapons have no effect
              case _ => cards.map( card => {
                generalEffectsHand.foldLeft(card.attributes)((currentAttributes, effectCard) => {
                  effectCard.getDungeonEffects.filter(_.isGeneralEffect).filter(_.matchesRequiredCard(card)).
                    foldLeft(currentAttributes)((a, e) => {
                      e.adjustAttributes(a)
                    })
                })
              })
            }
            val slotAttributes = combineAttributes(cardAttributes)
            applyIndividualEffects(List(monster), c => c.getBattleEffects, monster, applyIndividualEffects(
              cards, c => c.getDungeonEffects, monster, slotAttributes))
          })
          Right(resolveBattle(state, finalHand, monster, monsterIndex + 1, combineAttributes(attributes.filterNot(a =>
            a.contains("No Attack")))))
        } catch {
          case g: GameException => Left(GameError(g.getMessage))
        }
      case m => Left(GameError("Unexpected message " + m.getClass.getSimpleName))
    }
}

object Crawling {
  implicit val format: Format[Crawling] = Json.format
}


