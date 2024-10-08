package services

import controllers.GameException
import models.game.{BattleSlot, Card, GameError, HeroCard, ItemCard, MonsterCard, Player, SpellCard, State, WeaponCard}

import scala.annotation.tailrec
import scala.util.Random

object CardManager {
  def matchesType(card: Card, requiredType: String): Boolean = {
    (requiredType, card) match {
    case ("GoldValue", c) => c.hasGoldValue
    case ("Spell", _: SpellCard) => true
    case ("Hero", _: HeroCard) => true
    case ("Monster", _: MonsterCard) => true
    case ("Weapon", _: WeaponCard) => true
    case (name, c) if c.getName == name => true
    case (itemTraits, f: ItemCard) if itemTraits.split("\\+").forall(t => f.traits.contains(t)) => true
    case (heroTrait, h: HeroCard) if h.traits.contains(heroTrait) => true
    case (notHeroTrait, h: HeroCard)
      if notHeroTrait.startsWith("!") && !h.traits.contains(notHeroTrait.substring(1)) => true
    case ("Any", _) => true
    case _ => false
  }
}

  val HandSize = 6

  @tailrec
  private def fillHand(
                hand: List[Card],
                deck: List[Card],
                discard: List[Card],
                cards: Int,
                random: Random = new Random()
              ): (List[Card], List[Card], List[Card]) = {
    cards match {
      case 0 => (hand, deck, discard)
      case x if x < deck.length => (hand ::: deck.take(x), deck.drop(x), discard)
      case _ if discard.isEmpty => (hand ::: deck, Nil, Nil)
      case _ =>
        fillHand(hand ::: deck, random.shuffle(discard), Nil, cards - deck.length)
    }
  }

  def fillPlayerHand(player: Player, state: State): State = {
    val (newHand, newDeck, newDiscard) =
      fillHand(player.hand, player.deck, player.discard, CardManager.HandSize - player.hand.length)
    state.updatePlayer(player.userId)(_.copy(hand = newHand, deck = newDeck, discard = newDiscard))
  }

  def givePlayerCards(player: Player, cards: Int, state: State): (List[Card], State) = {
    val (newHand, newDeck, newDiscard) =
      fillHand(player.hand, player.deck, player.discard, cards)
    (
      newHand.drop(player.hand.length),
      state.updatePlayer(player.userId)(_.copy(hand = newHand, deck = newDeck, discard = newDiscard))
    )
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

  def getCardsFromHand(cardNames: List[String], playerId: Int, state: State): List[Card] = {
    val player = state.players.find(_.userId == playerId).get
    cardNames.foldLeft(player.hand, List[Card]())((soFar, name) => {
      val (hand, alreadyFound) = soFar
      hand.find(_.getName == name).map(c =>
        (CardManager.removeOneInstanceFromCards(hand, name), c :: alreadyFound)
      ).getOrElse((hand, alreadyFound))
    })._2
  }

  @tailrec
  def removeDestroyed(hand: List[Card], arrangement: List[BattleSlot], keepSelfDestroyed: Boolean): List[Card] = {
    arrangement match {
      case Nil => hand
      case x :: remaining =>
        removeDestroyed(
          x.destroyed.foldLeft(hand)((newHand, destroyedCard) => {
            if (destroyedCard != x.card || !keepSelfDestroyed)
              CardManager.removeOneInstanceFromCards(newHand, destroyedCard)
            else
              newHand
          }),
          remaining, keepSelfDestroyed
        )
    }
  }


}
