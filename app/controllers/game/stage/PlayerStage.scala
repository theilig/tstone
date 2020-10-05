package controllers.game.stage

import models.game.{Player, State}

abstract class PlayerStage() extends GameStage {
  def currentPlayerId: Int
  override def currentPlayer(state: State): Option[Player] = state.players.find(_.userId == currentPlayerId)
}
