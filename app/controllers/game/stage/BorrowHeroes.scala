package controllers.game.stage

import models.User
import models.game.{Borrow, Borrowed, GameError, Message, State}
import play.api.libs.json.{Format, Json}

import scala.annotation.tailrec

case class BorrowHeroes(currentPlayerId: Int, available: List[Borrowed], canPick: Int) extends PlayerStage {
  override def receive(message: Message, user: User, state: State): Either[GameError, State] = {
    @tailrec
    def processBorrowed(
                         available: List[Borrowed],
                         borrowedIndexes: List[Int],
                         currentIndex: Int,
                         currentState: State,
                         borrowed: List[Borrowed]
                       ): (State, List[Borrowed]) = {
      available match {
        case Nil => (currentState, borrowed)
        case x :: remaining if borrowedIndexes.contains(currentIndex) =>
          processBorrowed(
            remaining,
            borrowedIndexes,
            currentIndex + 1,
            currentState,
            x :: borrowed
          )
        case x :: remaining =>
          val newState = currentState.updatePlayer(x.userId)(p => p.copy(discard = x.card :: p.discard))
          processBorrowed(
            remaining,
            borrowedIndexes,
            currentIndex + 1,
            newState,
            borrowed
          )
      }
    }
    message match {
      case Borrow(borrowedIndexes) =>
        val (returnedState, borrowed) = processBorrowed(available, borrowedIndexes, 0, state, Nil)
        Right(returnedState.copy(currentStage = Crawling(currentPlayerId, borrowed = borrowed)))
      case _ => Left(GameError("Unexpected message"))
    }
  }
}

object BorrowHeroes {
  implicit val format: Format[BorrowHeroes] = Json.format
}
