package services

import com.google.inject.Inject
import controllers.GameException
import models.game.{Card, Player, State}

import scala.annotation.tailrec
import scala.util.Random

class CardManager @Inject() (random: Random) {
  def fillPlayerHand(player: Player, state: State): State = {
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
    val newPlayers = state.players.map {
      case p if p.userId == player.userId => p.copy(hand = newHand, deck = newDeck, discard = newDiscard)
      case p => p
    }
    state.copy(players = newPlayers)
  }

  def takeCard(player: Player, cardName: String, state: State): State = {
    val (newVillage, card) = state.village.get.takeCard(cardName)
    state.copy(village = Some(newVillage), players = state.players.map {
      case p if p.userId == player.userId && card.nonEmpty=> p.copy(discard = card.get :: p.discard)
      case p if p.userId == player.userId => throw new GameException(s"Can't find card $cardName")
      case p => p
    })
  }
}

object CardManager {
  val HandSize = 6
}
