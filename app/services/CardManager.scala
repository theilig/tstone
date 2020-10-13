package services

import controllers.GameException
import models.game.{Card, GameError, HeroCard, ItemCard, MonsterCard, Player, SpellCard, State}

import scala.annotation.tailrec
import scala.util.Random

object CardManager {
  def matchesType(card: Card, requiredType: String): Boolean = {
    (requiredType, card) match {
    case ("GoldValue", c) => c.hasGoldValue
    case ("Spell", _: SpellCard) => true
    case ("Hero", _: HeroCard) => true
    case ("Monster", _: MonsterCard) => true
    case (name, c) if c.getName == name => true
    case (itemTraits, f: ItemCard) if itemTraits.split("\\+").forall(t => f.traits.contains(t)) => true
    case (heroTrait, h: HeroCard) if h.traits.contains(heroTrait) => true
    case (notHeroTrait, h: HeroCard)
      if notHeroTrait.startsWith("!") && !h.traits.contains(notHeroTrait.substring(1)) => true
  }
}

  val HandSize = 6

  def fillPlayerHand(player: Player, state: State, random: Random = new Random()): State = {
    @tailrec
    def fillHand(hand: List[Card], deck: List[Card], discard: List[Card]): (List[Card], List[Card], List[Card]) = {
      CardManager.HandSize - hand.length match {
        case 0 => (hand, deck, discard)
        case x if x < deck.length => (hand ::: deck.take(x), deck.drop(x), discard)
        case _ if discard.isEmpty => (hand ::: deck, Nil, Nil)
        case _ =>
          fillHand(hand ::: deck, random.shuffle(discard), Nil)
      }
    }
    val (newHand, newDeck, newDiscard) = fillHand(player.hand, player.deck, player.discard)
    state.updatePlayer(player.userId)(_.copy(hand = newHand, deck = newDeck, discard = newDiscard))
  }

  def discardHand(player: Player, state: State): State = {
    fillPlayerHand(player.copy(hand = Nil, discard = player.discard ::: player.hand), state)
  }

  def takeCard(player: Player, cardName: String, state: State, topOnly: Boolean): (State, Card) = {
    val (newVillage, card) = state.village.get.takeCard(cardName, topOnly)
    if (card.isEmpty) {
      throw new GameException(s"Can't find card $cardName")
    }
    (state.copy(village = Some(newVillage)).updatePlayer(player.userId)(p => {
      p.copy(discard = card.get :: p.discard)
    }), card.get)
  }

  def removeOneInstanceFromCards[T <: Card](cards: List[T], targetCardName: String): List[T] = {
    cards match {
      case card :: restOfCards if card.getName == targetCardName => restOfCards
      case card :: restOfCards => card :: removeOneInstanceFromCards(restOfCards, targetCardName)
      case Nil => Nil
    }
  }

  def destroy(cardName: String, state: State): Either[GameError, State] = {
    val player = state.currentPlayer.get
    val possibleCard = player.hand.find(_.getName == cardName)
    if (possibleCard.nonEmpty) {
      Right(state.updatePlayer(player.userId)(p => {
        p.copy(hand = removeOneInstanceFromCards(player.hand, possibleCard.get.getName))
      }))
    } else {
      Left(GameError(s"$cardName was not found in hand"))
    }
  }
}
