package models.game

import controllers.game.stage.{GameStage, WaitingForPlayers}
import models.schema.Tables.GameRow
import play.api.libs.json._

case class State(players: List[Player], village: Option[Village], dungeon: Option[Dungeon], currentStage: GameStage) {
  def ownerId: Int = players.filterNot(_.pending).headOption.map(_.userId).getOrElse(0)
  def projection(userId: Int): State = {
    val dungeonProjection = dungeon match {
      case Some(d) if d.monsterPile.length > 3 => Some(d.copy(monsterPile = d.monsterPile.take(3) ::: CardBack :: Nil))
      case Some(d) => Some(d.copy(monsterPile = d.monsterPile.take(3)))
      case None => None
    }
    copy(
      players = players.map {
        case p if p.userId == userId => p.copy(discard = p.discard.take(1), deck = List(CardBack))
        case p if currentStage.currentPlayer == userId => p.copy(discard = p.discard.take(1), deck = List(CardBack))
        case p => p.copy(discard = p.discard.take(1), hand = p.hand.map(_ => CardBack), deck = List(CardBack))
      },
      dungeon = dungeonProjection
    )
  }
}

object State {
  implicit val stateFormat: Format[State] = Json.format[State]

  def apply(gameOwner: Player): State = {
    val players = List(gameOwner)
    new State(players, None, None, WaitingForPlayers)
  }

  def apply(gameRow: GameRow): State = {
    Json.parse(gameRow.state).as[State]
  }
}

