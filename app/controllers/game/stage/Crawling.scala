package controllers.game.stage

import controllers.GameException
import models.User
import models.game._
import play.api.libs.json.{Format, Json}
import services.{AttributeCalculator, CardManager}

case class Crawling(
                     currentPlayerId: Int,
                     banishes: Int = 0,
                     sendToBottoms: Int = 0,
                     borrowed: List[Borrowed] = List()
                   ) extends PlayerStage {
  def defeat(
              monster: MonsterCard, rank: Int, finalHand: List[Card], attributes: List[(String, Attributes)], state: State
            ): State = {
    val newDungeon = state.dungeon.map(_.defeat(rank))
    val cardsToTake = (rank, state.dungeon.get.ranks) match {
      case (1, Some(monster) :: Some(t: ThunderstoneCard) :: _) => monster :: t :: Nil
      case _ => monster :: Nil
    }
    val newState = getDiseases(monster, state.updatePlayer(currentPlayerId)(p =>
      p.copy(discard = cardsToTake ::: p.discard, hand = finalHand, xp = p.xp + monster.experiencePoints)
    ).copy(
      dungeon = newDungeon
    ))
    val (possibleDestroys, requiredCount) = getPossibleDestroys(monster, finalHand, attributes)
    val monsterSpoils = availableSpoils(monster :: Nil)
    if (possibleDestroys.length > requiredCount) {
      newState.copy(currentStage = Destroying(
        currentPlayerId,
        possibleDestroys,
        monsterSpoils = monsterSpoils,
        borrowed = borrowed
      ))
    } else if (possibleDestroys.nonEmpty) {
      val newHand = removeDestroyed(newState, possibleDestroys)
      checkSpoils(monsterSpoils, borrowed, newState.updatePlayer(currentPlayerId)(p => p.copy(hand = newHand)))
    } else {
      checkSpoils(monsterSpoils, borrowed, newState)
    }
  }

  def fightOff(
                monster: MonsterCard,
                rank: Int,
                finalHand: List[Card],
                attributes: List[(String, Attributes)],
                state: State
              ): State = {
    val newDungeon = state.dungeon.map(_.banish(rank))
    val newState = getDiseases(monster, state.updatePlayer(currentPlayerId)(p =>
      p.copy(hand = finalHand)
    ).copy(
      dungeon = newDungeon
    ))
    val (possibleDestroys, requiredCount) = getPossibleDestroys(monster, finalHand, attributes)
    if (possibleDestroys.length > requiredCount) {
      newState.copy(currentStage = Destroying(currentPlayerId, possibleDestroys, monsterSpoils = Nil, borrowed))
    } else if (possibleDestroys.nonEmpty) {
      val newHand = removeDestroyed(newState, possibleDestroys)
      endTurn(newState.updatePlayer(currentPlayerId)(p => p.copy(hand = newHand)))
    } else {
      endTurn(newState)
    }
  }

  private def removeDestroyed(newState: State, destroyed: List[Card]) = {
    val newHand = destroyed.foldLeft(newState.currentPlayer.get.hand)((h, destroy) => {
      CardManager.removeOneInstanceFromCards(h, destroy.getName)
    })
    newHand
  }

  private def getPossibleDestroys(
                                   monster: MonsterCard, finalHand: List[Card], attributes: List[(String, Attributes)]
                                 ): (List[Card], Int) = {
    val destroyEffects = monster.battleEffects.filter(_.isDestroy)
    val possibleDestroys = destroyEffects.foldLeft(List[Card]())((soFar, effect) => {
      soFar ::: finalHand.filter(c => effect.matchesRequiredCard(c, attributes))
    })
    if (destroyEffects.exists(_.repeatable)) {
      (possibleDestroys, possibleDestroys.length)
    } else if (possibleDestroys.nonEmpty) {
      (possibleDestroys, 1)
    }else {
      (Nil, 0)
    }
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
                     attributes: List[(String, Attributes)]
                   ): State = {
    val adjustedAttributes = AttributeCalculator.finalAttributes(finalHand, Some(monster), rank, attributes.map(_._2))
    if (adjustedAttributes.contains("No Attack")) {
      throw new GameException("You can't attack this monster")
    }
    val lightPenalty = -Math.min(0, adjustedAttributes.getOrElse("Light", 0)) * state.dungeon.get.lightPenalty
    if (
      adjustedAttributes.getOrElse("Attack", 0) + adjustedAttributes.getOrElse("Magic Attack", 0) >=
      monster.health + lightPenalty
    ) {
      defeat(monster, rank, finalHand, attributes, state)
    } else {
      fightOff(monster, rank, finalHand, attributes, state)
    }
  }

  def combineAttributes(attributes: Iterable[Attributes]): Attributes = {
    attributes.foldLeft(Map[String, Int]())((a, b) => {
      b.foldLeft(a)((a, pair) => {
        val (key, value) = pair
        a + (key -> (value + a.getOrElse(key, 0)))
      })
    })
  }

  def effectCount(effect: String, state: State): Int = {
    currentPlayer(state).get.hand.map(c => {
      c.getDungeonEffects.count(p => p.effect.contains(effect))
    }).sum
  }

  def receive(message: Message, user: User, state: State): Either[GameError, State] =
    message match {
      case Battle(monsterIndex, cardArrangement) =>
        val hand = currentPlayer(state).get.hand
        try {
          val monster = state.dungeon.get.ranks.drop(monsterIndex).head match {
            case Some(m: MonsterCard) => m
            case _ => throw new GameException("Invalid monster to battle")
          }
          val generalEffects = (
            monster.battleEffects ::
              CardManager.removeDestroyed(hand, cardArrangement, keepSelfDestroyed = true).map(
               _.getDungeonEffects
              )
            ).flatten.filter(_.isGeneralEffect)

          val finalHand = CardManager.removeDestroyed(hand, cardArrangement, keepSelfDestroyed = false)
          val attributes: List[(String, Attributes)] = cardArrangement.map(slot => {
            slot.copy(hand = Some(hand)).battleAttributes(generalEffects, Some(monster), monsterIndex + 1)
          })
          Right(resolveBattle(state, finalHand, monster, monsterIndex + 1, attributes))
        } catch {
          case g: GameException => Left(GameError(g.getMessage))
        }
      case b : Banish if effectCount("Banish", state) > banishes =>
        val newOrder = b.cards(state.dungeon.get)
        val ranksSize = state.dungeon.get.ranks.length
        if (newOrder.flatten.length == state.dungeon.get.ranks.flatten.length) {
          // if there is a card at the end put it at the bottom of the monster pile
          val newMonsterPile = state.dungeon.get.monsterPile ::: newOrder.drop(ranksSize).flatten
          val newState = state.copy(dungeon = Some(Dungeon(newMonsterPile, newOrder.take(ranksSize))))
          val destroyedState = newState.updatePlayer(currentPlayerId)(p =>
            p.copy(hand = CardManager.removeOneInstanceFromCards(p.hand, b.destroyed))
          )
          val filledHandState =
            CardManager.givePlayerCards(destroyedState.currentPlayer.get, 1, destroyedState)._2
          Right(newState.dungeon.get.fill(filledHandState).copy(currentStage = copy(banishes = banishes + 1)))
        } else {
          Left(GameError("Invalid Dungeon Order"))
        }
      case s: SendToBottom if effectCount("SendToBottom", state) > sendToBottoms =>
        val newMonsterPile = state.dungeon.get.monsterPile :::
          state.dungeon.get.ranks.slice(s.banished, s.banished + 1).flatten
        val newDungeonState = state.copy(dungeon = state.dungeon.map(d => d.copy(
          monsterPile = newMonsterPile,
          ranks = d.ranks.take(s.banished) ::: None :: d.ranks.drop(s.banished + 1)))
        )
        Right(newDungeonState.dungeon.get.fill(newDungeonState))
      case Destroy(cardNames, _) => processDestroyDrawing(cardNames, c => c.getDungeonEffects, state)
      case m => Left(GameError("Unexpected message " + m.getClass.getSimpleName))
    }
}

object Crawling {
  implicit val format: Format[Crawling] = Json.format
}


