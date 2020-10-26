package controllers.game.stage

import models.game.{Card, GameError, MonsterCard, Player, State, ThunderstoneCard}
import services.CardManager

abstract class PlayerStage extends GameStage {
  def currentPlayerId: Int
  override def currentPlayer(state: State): Option[Player] = state.players.find(_.userId == currentPlayerId)
  def endTurn(state: State): State = {
    def borrowed: Map[Int, String] = {
      val borrowedWithStringKey: Map[String, String] = state.currentStage match {
        case c: Crawling => c.borrowed
        case b: BorrowHeroes => b.borrowed
        case _ => Map()
      }
      borrowedWithStringKey.map(p => p._1.toInt -> p._2)
    }

    def returnCard(state: State, pair: (Int, String)): State = {
      CardManager.getCardsFromHand(List(pair._2), currentPlayerId, state).map(c => {
        val currentPlayerState = state.updatePlayer(currentPlayerId)(p =>
          p.copy(hand = CardManager.removeOneInstanceFromCards(p.hand, pair._2))
        )
        currentPlayerState.updatePlayer(pair._1)(p => p.copy(discard = c :: p.discard))
      }).headOption.getOrElse(state)
    }
    val filledState = state.dungeon.get.fill(state)

    val gameOver = filledState.dungeon.get.ranks.head match {
      case Some(_ : ThunderstoneCard) => true
      case _ => !(filledState.dungeon.get.ranks.flatten ::: filledState.dungeon.get.monsterPile).exists {
        case _ : ThunderstoneCard => true
        case _ => false
      }
    }
    if (gameOver) {
      filledState.copy(currentStage = GameEnded)
    } else {
      val returnedState =
        borrowed.foldLeft(filledState)(returnCard)
      CardManager.discardHand(currentPlayer(returnedState).get, returnedState).copy(
        currentStage = ChoosingDestination(nextPlayer(returnedState).userId)
      )
    }
  }
  def nextPlayer(state: State): Player = {
    @scala.annotation.tailrec
    def after(player: Player, players: List[Player]): Option[Player] = {
      players match {
        case Nil => None
        case p :: rest if p.userId == player.userId => rest.headOption
        case _ :: rest => after(player, rest)
      }
    }
    after(currentPlayer(state).get, state.players).getOrElse(state.players.head)
  }

  def checkSpoils(monsterSpoils: List[String], state: State): State = {
    // only take monster card spoils if it's the monster we just killed
    val spoils = monsterSpoils ::: availableSpoils(state.currentPlayer.get.hand.filterNot({
      case _ : MonsterCard => true
      case _ => false
    }))
    spoils match {
      case Nil => endTurn(state)
      case "DiscardOrDestroy" :: Nil =>
        val newHandState = CardManager.discardHand(state.currentPlayer.get, state)
        newHandState.copy(currentStage = DiscardOrDestroy(currentPlayerId, newHandState.currentPlayer.get.hand))
      case spoils => state.copy(currentStage = TakingSpoils(currentPlayerId, spoils))
    }
  }

  def availableSpoils(cards: List[Card]): List[String] = {
    val spoilsEffects = cards.flatMap(c =>
      c.getBattleEffects
    ).filter(e => e.effect.contains("Spoils") && !e.requiredType.contains("Disease"))
    val spoils = spoilsEffects.map(_.requiredType.get)
    spoils
  }

  def destroyCards(cardNames: List[String], state: State, finalTransform: State => State): Either[GameError, State] = {
    val initial: Either[GameError, State] = Right(state)
    cardNames.foldLeft(initial)((currentState, cardName) => {
      currentState.flatMap(s => CardManager.destroy(cardName, s))
    }).map(finalState => finalTransform(finalState))

  }

  def removePlayerFromList(playerIds: List[Int], playerId: Int): List[Int] = {
    playerIds.foldLeft(List[Int]())((soFar, p) => {
      if (p == playerId) {
        soFar
      } else {
        p :: soFar
      }
    })
  }

}
