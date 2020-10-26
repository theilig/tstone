package models.game

import controllers.game.stage.{GameEnded, GameStage, WaitingForPlayers}
import models.schema.Tables.GameRow
import play.api.libs.json._

case class State(players: List[Player], village: Option[Village], dungeon: Option[Dungeon], currentStage: GameStage) {
  def ownerId: Int = players.filterNot(_.pending).headOption.map(_.userId).getOrElse(0)

  def projection(userId: Int): JsObject = {
    currentStage match {
      case GameEnded => JsObject(
        Seq(
          "players" -> Json.toJson(players),
          "currentStage" -> Json.toJson(currentStage)
        )
      )
      case _ =>
        val playerList = players.map {
          case p if p.userId == userId => JsObject(
            Seq(
              "discard" -> Json.toJson(p.discard.take(1)),
              "hand" -> Json.toJson(p.hand),
              "attributes" -> Json.toJson(p.attributes),
              "userId" -> JsNumber(p.userId),
              "xp" -> JsNumber(p.xp)
            )
          )
          case p => JsObject(Seq("userId" -> JsNumber(p.userId)))
        }
        val monsterPile: List[Card] = if (dungeon.get.monsterPile.nonEmpty) {
          CardBack :: Nil
        } else {
          Nil
        }
        val ranks = dungeon.get.ranks.map({
          case Some(m) => m
          case None => CardBack
        })
        val dungeonProjection = JsObject(
          Seq(
            "ranks" -> Json.toJson(ranks),
            "monsterPile" -> Json.toJson(monsterPile)
          )
        )
        JsObject(
          Seq(
            "players" -> JsArray(playerList),
            "village" -> Json.toJson(village),
            "dungeon" -> dungeonProjection,
            "currentStage" -> Json.toJson(currentStage)
          )
        )
    }
  }
  def updatePlayer(userId: Int)(transform: Player => Player): State = {
    copy(players = players.map {
      case p if p.userId == userId => transform(p)
      case p => p
    })
  }
  def currentPlayer: Option[Player] = currentStage.currentPlayer(this)
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

