package controllers.game.stage

import models.game.{Player, State}
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
}
