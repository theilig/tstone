package controllers.game.stage

import models.game.{Card, GameError, MonsterCard, Player, State, ThunderstoneCard}
import services.CardManager

abstract class PlayerStage extends GameStage {
  def currentPlayerId: Int
  override def currentPlayer(state: State): Option[Player] = state.players.find(_.userId == currentPlayerId)
  def endTurn(state: State): State = {
    val gameOver = state.dungeon.get.ranks.head match {
      case Some(t : ThunderstoneCard) => true
      case _ => !(state.dungeon.get.ranks.flatten ::: state.dungeon.get.monsterPile).exists {
        case _ : ThunderstoneCard => true
        case _ => false
      }
    }
    if (gameOver) {
      state.copy(currentStage = GameEnded)
    } else {
      val filledState = state.dungeon.get.fill(state)
      CardManager.discardHand(currentPlayer(filledState).get, filledState).copy(
        currentStage = ChoosingDestination(nextPlayer(filledState).userId)
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
