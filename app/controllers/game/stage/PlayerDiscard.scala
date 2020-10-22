package controllers.game.stage

import models.User
import models.game.{Discard, GameError, Message, State}
import services.CardManager

case class PlayerDiscard(currentPlayerId: Int, playerIds: List[Int], howMany: Int, breach: Boolean) extends PlayerStage {
  override def receive(message: Message, user: User, state: State): Either[GameError, State] = message match {
      case Discard(cardNames) if playerIds.contains(user.userId) =>
        val cards = CardManager.getCardsFromHand(cardNames, user.userId, state)
        if (cards.length == cardNames.length) {
          val newState = state.updatePlayer(user.userId)(player => {
            val newHand = cardNames.foldLeft(player.hand)((h, name) => {
              CardManager.removeOneInstanceFromCards(h, name)
            })
            player.copy(hand = newHand, discard = cards ::: player.discard)
          })
          val playersLeft = removePlayerFromList(playerIds, user.userId)
          if (playersLeft.nonEmpty) {
            Right(newState.copy(currentStage = copy(playerIds = playersLeft)))
          } else if (breach) {
            Right(endTurn(newState))
          } else {
            Right(newState.copy(currentStage = Crawling(currentPlayerId)))
          }
        } else {
          Left(GameError("Couldn't find cards"))
        }
      case _ => Left(GameError("Unexpected message"))
    }
}


