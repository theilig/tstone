package services

import controllers.GameException
import models.game.{Card, GameError, Player, State}

import scala.annotation.tailrec
import scala.util.Random

object CardManager {
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

  def takeCard(player: Player, cardName: String, state: State): State = {
    val (newVillage, card) = state.village.get.takeCard(cardName)
    if (card.isEmpty) {
      throw new GameException(s"Can't find card $cardName")
    }
    state.copy(village = Some(newVillage)).updatePlayer(player.userId)(p => {
      p.copy(discard = card.get :: p.discard)
    })
  }

  def removeOneInstanceFromCards[T <: Card](cards: List[T], targetCard: Card): List[T] = {
    cards match {
      case card :: restOfCards if card.getName == targetCard.getName => restOfCards
      case card :: restOfCards => card :: removeOneInstanceFromCards(restOfCards, targetCard)
      case Nil => Nil
    }
  }

  def destroy(player: Player, cardName: String, state: State): Either[GameError, State] = {
    val possibleCard = player.hand.find(_.getName == cardName)
    if (possibleCard.nonEmpty) {
      Right(state.updatePlayer(player.userId)(p => {
        p.copy(hand = removeOneInstanceFromCards(player.hand, possibleCard.get))
      }))
    } else {
      Left(GameError(s"$cardName was not found in hand"))
    }
  }
}
