package controllers.game.stage

import models.User
import models.game.{Borrowed, Card, Discard, GameError, HeroCard, Message, State}
import play.api.libs.json.{Format, Json}
import services.CardManager

case class PlayerDiscard(
                          currentPlayerId: Int,
                          playerIds: Set[Int],
                          borrowedHeroes: List[Borrowed] = List(),
                          howMany: Int,
                          borrows: Int = 0,
                          heroesDouble: Int = 0,
                          isBreach: Boolean = false
                        )
  extends PlayerStage {
  override def receive(message: Message, user: User, state: State): Either[GameError, State] = message match {
      case Discard(borrows, discards) if playerIds.contains(user.userId) =>
        val possiblePlayer = state.players.find(_.userId == user.userId).filter(p => playerIds.contains(p.userId))
        possiblePlayer.map(p => {
          val borrowedCards = CardManager.getCardsFromHand(borrows, user.userId, state)
          val newHand = borrows.foldLeft(p.hand)((h, cardName) => {
            CardManager.removeOneInstanceFromCards(h, cardName)
          })
          val discardedCards = CardManager.getCardsFromHand(discards, user.userId, state)
          val finalHand = discards.foldLeft(newHand)((h, cardName) => {
            CardManager.removeOneInstanceFromCards(h, cardName)
          })
          val discardedHeroCount = discardedCards.collect {
            case _: HeroCard => true
          }.length
          if (borrowedCards.length < borrows.length) {
            Left(GameError("Couldn't find borrowed cards"))
          } else if (!borrowedCards.forall {
            case _: HeroCard => true
            case _ => false
          }) {
            Left(GameError("Only Heroes can be borrowed"))
          } else if (discardedCards.length < discards.length) {
            Left(GameError("Couldn't find all discards"))
          } else if (howMany !=
            borrowedCards.length + discardedCards.length + Math.min(heroesDouble, discardedHeroCount)) {
            Left(GameError("Wrong mix of cards"))
          } else {
            val discardedState = state.updatePlayer(user.userId)(
              p => p.copy(hand = finalHand, discard = discardedCards ::: p.discard)
            )
            val newDiscardStage = copy(playerIds = playerIds.filterNot(_ == user.userId),
              borrowedHeroes = borrowedHeroes ::: borrowedCards.map(c => Borrowed(user.userId, c))
            )
            val newStage = if (newDiscardStage.playerIds.nonEmpty) {
              newDiscardStage
            } else if (newDiscardStage.borrowedHeroes.length <= newDiscardStage.borrows) {
                Crawling(currentPlayerId = currentPlayerId, borrowed = borrowedHeroes)
            } else {
                BorrowHeroes(currentPlayerId, newDiscardStage.borrowedHeroes, newDiscardStage.borrows)
            }
            Right(discardedState.copy(currentStage = newStage))
          }
        }).getOrElse(Left(GameError("Couldn't find user in game")))
      case _ => Left(GameError("Unexpected message"))
    }
}

object PlayerDiscard {
  implicit val format: Format[PlayerDiscard] = Json.format
}



