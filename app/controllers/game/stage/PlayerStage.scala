package controllers.game.stage

import models.game.{GameError, Player, State}
import services.CardManager

abstract class PlayerStage() extends GameStage {
  def currentPlayerId: Int
  override def currentPlayer(state: State): Option[Player] = state.players.find(_.userId == currentPlayerId)
  def endTurn(state: State): State = {
    CardManager.discardHand(currentPlayer(state).get, state).copy(
      currentStage = ChoosingDestination(nextPlayer(state).userId)
    )
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

  def checkSpoils(state: State): State = {
    val spoils = state.currentPlayer.get.hand.flatten(c =>
      c.getBattleEffects
    ).foldLeft(List[String]())((soFar, effect) => {
      effect.spoils.map(s => s :: soFar).getOrElse(soFar)
    })
    if (spoils.nonEmpty) {
      state.copy(currentStage = TakingSpoils(currentPlayerId, spoils))
    } else {
      endTurn(state)
    }

  }

  def destroyCards(cardNames: List[String], state: State, finalTransform: State => State): Either[GameError, State] = {
    val initial: Either[GameError, State] = Right(state)
    cardNames.foldLeft(initial)((currentState, cardName) => {
      currentState.flatMap(s => CardManager.destroy(cardName, s))
    }).map(finalState => finalTransform(finalState))

  }
}
