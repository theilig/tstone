package controllers.game.stage

import models.User
import models.game.{GameError, HeroCard, Message, State, Upgrade}
import play.api.libs.json.{Format, Json}
import services.CardManager

case class Upgrading(currentPlayerId: Int) extends PlayerStage {
  def upgradeCards(upgrades: Map[String, String], state: State): Either[GameError, State] = {
    val initial: Either[GameError, State] = Right(state)
    upgrades.foldLeft(initial)((current, upgrade) => {
      current.fold(
        e => Left(e),
        s => {
        val (oldCardName, newCardName) = upgrade
        val oldCard = s.currentPlayer.get.hand.find(_.getName == oldCardName)
        val (newVillage, newCard) = s.village.get.takeCard(newCardName)
        (oldCard, newCard) match {
          case (Some(oldHero: HeroCard), Some(newHero: HeroCard))
            if oldHero.upgradeCost.exists(_ <= s.currentPlayer.get.xp) =>
            Right(
              s.updatePlayer(currentPlayerId)(p => p.copy(
                hand = CardManager.removeOneInstanceFromCards(p.hand, oldCardName),
                discard = newHero :: p.discard,
                xp = p.xp - oldHero.upgradeCost.get
              )).copy(village = Some(newVillage))
            )
          case (None, _) => Left(GameError(s"Can't find $oldCardName"))
          case (_, None) => Left(GameError(s"Can't find $newCardName"))
          case (Some(_ : HeroCard), _) => Left(GameError(s"not enough experience to upgrade"))
          case _ => Left(GameError(s"Upgrade Failed"))
        }
      })
    })
  }


  def receive(message: Message, user: User, state: State): Either[GameError, State] =
    message match {
      case Upgrade(upgrades) => upgradeCards(upgrades, state).map(
        s => endTurn(s)
      )
      case m => Left(GameError("Unexpected message " + m.getClass.getSimpleName))
    }
}

object Upgrading {
  implicit val format: Format[Upgrading] = Json.format
}

