package controllers.game.stage

import controllers.GameException
import models.User
import models.game._
import play.api.libs.json.{Format, Json}
import services.CardManager

import scala.annotation.tailrec

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

  def defeat(monster: MonsterCard, rank: Int, finalHand: List[Card], state: State): State = {
    val newDungeon = state.dungeon.map(_.defeat(rank))
    val newState = getDiseases(monster, state.updatePlayer(currentPlayerId)(p =>
      p.copy(discard = monster :: p.discard, hand = finalHand, xp = p.xp + monster.experiencePoints)
    ).copy(
      dungeon = newDungeon
    ))
    val possibleDestroys = getPossibleDestroys(monster, finalHand)
    val monsterSpoils = availableSpoils(monster :: Nil)
    if (possibleDestroys.length > 1) {
      newState.copy(currentStage = Destroying(
        currentPlayerId,
        possibleDestroys,
        monsterSpoils = monsterSpoils))
    } else if (possibleDestroys.length == 1) {
      val newHand = CardManager.removeOneInstanceFromCards(
        newState.currentPlayer.get.hand,
        possibleDestroys.head.getName
      )

      checkSpoils(monsterSpoils, newState.updatePlayer(currentPlayerId)(p => p.copy(hand = newHand)))
    } else {
      checkSpoils(monsterSpoils, newState)
    }
  }

  def fightOff(monster: MonsterCard, rank: Int, finalHand: List[Card], state: State): State = {
    val newDungeon = state.dungeon.map(_.banish(rank))
    val newState = getDiseases(monster, state.updatePlayer(currentPlayerId)(p =>
      p.copy(hand = finalHand)
    ).copy(
      dungeon = newDungeon
    ))
    val possibleDestroys: List[Card] = getPossibleDestroys(monster, finalHand)
    if (possibleDestroys.length > 1) {
      newState.copy(currentStage = Destroying(currentPlayerId, possibleDestroys, monsterSpoils = Nil))
    } else if (possibleDestroys.length == 1) {
      val newHand = CardManager.removeOneInstanceFromCards(
        newState.currentPlayer.get.hand,
        possibleDestroys.head.getName
      )
      endTurn(newState.updatePlayer(currentPlayerId)(p => p.copy(hand = newHand)))
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
    val combinedEffects = (finalHand.flatten(c => c.getDungeonEffects) ::: monster.battleEffects).filter(_.isCombined)
    val adjustedAttributes = combinedEffects.foldLeft(lightAdjustedAttributes)((a, e) => {
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
    if (
      adjustedAttributes.getOrElse("Attack", 0) + adjustedAttributes.getOrElse("Magic Attack", 0) >=
      monster.health + lightPenalty
    ) {
      defeat(monster, rank, finalHand, state)
    } else {
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

  def banishCount(state: State): Int = {
    currentPlayer(state).get.hand.map(c => {
      c.getDungeonEffects.count(p => p.effect.contains("SendToBottom"))
    }).sum
  }
  def receive(message: Message, user: User, state: State): Either[GameError, State] =
    message match {
      case Battle(monsterIndex, cardArrangement) =>
        val hand = currentPlayer(state).get.hand
        try {
          val monster = state.dungeon.get.monsterPile.drop(monsterIndex).headOption match {
            case Some(m: MonsterCard) => m
            case _ => throw new GameException("Invalid monster to battle")
          }
          val generalEffects = (
            monster.battleEffects ::
              removeDestroyed(hand, cardArrangement, includeSelfDestroyed = true).map(
               _.getDungeonEffects
              )
            ).flatten.filter(_.isGeneralEffect)

          val finalHand = removeDestroyed(hand, cardArrangement, includeSelfDestroyed = false)
          val attributes: List[Map[String, Int]] = cardArrangement.map(slot => {
            slot.copy(hand = Some(hand)).battleAttributes(generalEffects, monster, monsterIndex + 1)
          })
          Right(resolveBattle(state, finalHand, monster, monsterIndex + 1, combineAttributes(attributes.filterNot(a =>
            a.contains("No Attack")))))
        } catch {
          case g: GameException => Left(GameError(g.getMessage))
        }
      case Banish(banished) =>
        @tailrec
        def banishedMonsters(pile: List[Card], names: List[String], soFar: List[Card]): List[Card] = {
          names match {
            case x :: remaining if pile.exists(_.getName == x) =>
              banishedMonsters(
                CardManager.removeOneInstanceFromCards(pile, x),
                remaining,
                pile.find(_.getName == x).get :: soFar
              )
            case _ :: remaining =>
              banishedMonsters(pile, remaining, soFar)
            case Nil => soFar
          }
        }
        val monstersBanished = banishedMonsters(state.dungeon.get.monsterPile.take(3), banished, Nil)
        if (monstersBanished.length == banished.length && banishCount(state) >= banished.length) {
          val newDungeonPile = banished.foldLeft(state.dungeon.get.monsterPile)((pile, name) => {
            CardManager.removeOneInstanceFromCards(pile, name) ::: monstersBanished
          })
          Right(state.copy(dungeon = state.dungeon.map(d => d.copy(monsterPile = newDungeonPile))))
        } else {
          Left(GameError("Banish failed"))
        }
      case m => Left(GameError("Unexpected message " + m.getClass.getSimpleName))
    }
}

object Crawling {
  implicit val format: Format[Crawling] = Json.format
}


