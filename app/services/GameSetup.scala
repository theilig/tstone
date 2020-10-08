package services

import com.google.inject.Inject
import controllers.game.stage.ChoosingDestination
import dao.CardDao
import models.game.{Dungeon, Player, State, Village}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class GameSetup @Inject() (cardDao: CardDao)(implicit ec: ExecutionContext) {
  def setupGame(state: State): Future[State] = {
    val random = new Random
    val startingCards: List[String] = List("Militia", "Iron Rations", "Dagger", "Torch", "Disease")
    val randomizedState = for {
      village <- Village.build(startingCards, cardDao)
      dungeon <- Dungeon.build(cardDao)
    } yield state.copy(village = Some(village), dungeon = Some(dungeon))
    randomizedState.map(state => {
      val startingPlayerIndex = random.between(0, state.players.length)
      dealStartingCards(state).copy(currentStage = ChoosingDestination(state.players.drop(startingPlayerIndex).head.userId))

    })
  }

  def dealStartingCards(state: State): State = {
    def dealPlayers(players: List[Player], cards: List[String], state: State): State = {
      def dealCardsToPlayer(player: Player, cards: List[String], state: State): State = {
        cards.foldLeft(state)((s, c) => CardManager.takeCard(player, c, s, topOnly = false)._1)
      }
      players.foldLeft(state)((s, p) => dealCardsToPlayer(p, cards, s))
    }
    def fillPlayersHands(state: State): State = {
      state.players.foldLeft(state)((s, p) => CardManager.fillPlayerHand(p, s))
    }
    val startingDeck =
      List.fill(6)("Militia") :::
        List.fill(2)("Torch") :::
        List.fill(2)("Iron Rations") :::
        List.fill(2)("Dagger")
    fillPlayersHands(dealPlayers(state.players, startingDeck, state))
  }
}

