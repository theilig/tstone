package controllers.game.stage

import models.User
import models.game.{GameError, Loan, Message, State}
import play.api.libs.json.{Format, Json}
import services.CardManager

case class BorrowHeroes(currentPlayerId: Int, playerIds: List[Int], borrowed: Map[String, String]) extends PlayerStage {
  override def receive(message: Message, user: User, state: State): Either[GameError, State] =
    message match {
      case Loan(hero) if playerIds.contains(user.userId) =>
        state.players.find(_.userId == user.userId).flatMap(donor => {
          donor.hand.find(_.getName == hero).map(card => {
            val updatedBorrowed = borrowed + (user.userId.toString -> card.getName)
            val currentPlayerState = state.updatePlayer(currentPlayerId)(p => p.copy(
              hand = card :: p.hand
            )).copy(currentStage = copy(borrowed = updatedBorrowed))
            val updatedState = currentPlayerState.updatePlayer(donor.userId)(p => p.copy(hand =
              CardManager.removeOneInstanceFromCards(p.hand, hero)))
            val playersLeft = removePlayerFromList(playerIds, user.userId)
            if (playersLeft.nonEmpty) {
              updatedState.copy(currentStage = copy(playerIds = playersLeft, borrowed = updatedBorrowed))
            } else {
              updatedState.copy(currentStage = Crawling(currentPlayerId, borrowed = updatedBorrowed))
            }
          })
        }).map(Right(_)).getOrElse(Left(GameError("Invalid Loan")))
    }
}

object BorrowHeroes {
  implicit val format: Format[BorrowHeroes] = Json.format
}
