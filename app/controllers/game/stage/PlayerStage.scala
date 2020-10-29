package controllers.game.stage

import models.game.{Borrowed, Card, GameError, MonsterCard, Player, State, ThunderstoneCard, TurnEffect}
import services.CardManager

abstract class PlayerStage extends GameStage {
  def currentPlayerId: Int
  override def currentPlayer(state: State): Option[Player] = state.players.find(_.userId == currentPlayerId)
  def endTurn(state: State): State = {
    val filledState = state.dungeon.get.fill(state)

    val gameOver = filledState.dungeon.get.ranks.head match {
      case Some(_ : ThunderstoneCard) => true
      case _ => !(filledState.dungeon.get.ranks.flatten ::: filledState.dungeon.get.monsterPile).exists {
        case _ : ThunderstoneCard => true
        case _ => false
      }
    }
    if (gameOver) {
      filledState.copy(currentStage = GameEnded)
    } else {
      val borrowed: List[Borrowed] = filledState.currentStage match {
        case Crawling(_, _, _, b) => b
        case TakingSpoils(_, _, b) => b
        case Destroying(_, _, _, b) => b
        case _ => Nil
      }
      val returnedState = borrowed.foldLeft(filledState)((s, borrow) => {
        s.updatePlayer(borrow.userId)(p => p.copy(discard = borrow.card :: p.discard))
      })
      CardManager.discardHand(currentPlayer(returnedState).get, returnedState).copy(
        currentStage = ChoosingDestination(nextPlayer(returnedState).userId)
      )
    }
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

  def checkSpoils(monsterSpoils: List[String], borrowed: List[Borrowed], state: State): State = {
    // only take monster card spoils if it's the monster we just killed
    val spoils = monsterSpoils ::: availableSpoils(state.currentPlayer.get.hand.filterNot({
      case _ : MonsterCard => true
      case _ => false
    }))
    spoils match {
      case Nil => endTurn(state)
      case "DiscardOrDestroy" :: Nil =>
        val newHandState = CardManager.discardHand(state.currentPlayer.get, state)
        newHandState.copy(currentStage = DiscardOrDestroy(currentPlayerId, newHandState.currentPlayer.get.hand))
      case spoils => state.copy(currentStage = TakingSpoils(currentPlayerId, spoils, borrowed))
    }
  }

  def availableSpoils(cards: List[Card]): List[String] = {
    val spoilsEffects = cards.flatMap(c =>
      c.getBattleEffects
    ).filter(e => e.effect.contains("Spoils") && !e.requiredType.contains("Disease"))
    val spoils = spoilsEffects.map(_.requiredType.get)
    spoils
  }

  def destroyCards(cardNames: List[String], state: State, finalTransform: State => State): Either[GameError, State] = {
    val initial: Either[GameError, State] = Right(state)
    cardNames.foldLeft(initial)((currentState, cardName) => {
      currentState.flatMap(s => CardManager.destroy(cardName, s))
    }).map(finalState => finalTransform(finalState))
  }

  def processDestroyDrawing(
                             cardsDestroyed: Map[String, List[String]],
                             getEffects: Card => List[TurnEffect],
                             state: State
                           ): Either[GameError, State] = {
    val initialResult: Either[GameError, State] = Right(state)
    cardsDestroyed.foldLeft(initialResult)((currentResult, destroy) => {
      currentResult.fold(
        e => Left(e),
        s => {
          val (destroyer, destroyed) = destroy
          val destroyedCards = destroyed.flatMap(name => s.currentPlayer.get.hand.find(_.getName == name))
          val possibleEffect = s.currentPlayer.get.hand.find(_.getName == destroyer).flatMap(destroyerCard => {
            getEffects(destroyerCard).find(e => e.effect.contains("Destroy") && e.adjustment.exists(
              p => p.attribute == "Card" &&
                destroyedCards.forall(c => e.matchesRequiredCard(c, c.getName == destroyerCard.getName))))
          })
          possibleEffect match {
            case Some(effect) =>
              val updatedHand = destroyed.foldLeft(s.currentPlayer.get.hand)((adjustedHand, cardName) => {
                CardManager.removeOneInstanceFromCards(adjustedHand, cardName)
              })
              if (updatedHand.length + destroyed.length == s.currentPlayer.get.hand.length) {
                val destroyedState = s.updatePlayer(currentPlayerId)(p => p.copy(hand = updatedHand))
                Right(CardManager.givePlayerCards(
                  destroyedState.currentPlayer.get,
                  effect.adjustment.get.amount * destroyed.length,
                  destroyedState)._2
                )
              } else {
                Left(GameError("Couldn't find all destroyed cards in hand"))
              }
            case None => Left(GameError(s"Couldn't destroy all cards with $destroyer"))
          }
        })
    })
  }

}
