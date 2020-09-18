package services

import com.google.inject.Inject
import controllers.game.stage.PickDestination
import dao.CardDao
import models.game.{Dungeon, State, Village}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class GameSetup @Inject() (cardDao: CardDao)(implicit ec: ExecutionContext) {
  def setupGame(state: State): Future[State] = {
    val random = new Random
    val startingCards: List[String] = List("Militia", "Iron Rations", "Dagger", "Torch")
    val randomizedState = for {
      village <- Village.build(startingCards, cardDao)
      dungeon <- Dungeon.build(cardDao)
    } yield state.copy(village = Some(village), dungeon = Some(dungeon))
    randomizedState.map(state => {
      dealStartingCards(state).copy(currentStage = PickDestination(random.between(0, state.players.length)))
    })
  }

  def giveEachPlayer(state: State, cardName: String): State = {
    val pile = state.village.get.findPile(cardName)
    val newPlayers = state.players.map(p => {
      p.copy(discard = pile.takeTopCard :: p.discard)
    })
    state.copy(players = newPlayers)
  }

  def dealStartingCards(state: State): State = {
    var newState = state
    val startingDeck =
      List.fill(6)("Militia") :::
        List.fill(2)("Torch") :::
        List.fill(2)("Iron Rations") :::
        List.fill(2)("Dagger")
    startingDeck.foreach(cardName => {
      newState = giveEachPlayer(state, cardName)
    })
    newState
  }
}

