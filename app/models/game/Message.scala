package models.game

import controllers.game.stage.{BorrowHeroes, Destroying, PlayerDiscard, PlayerStage, Purchasing, Resting, TakingSpoils}
import models.User
import play.api.libs.json.{Format, JsError, JsObject, JsPath, JsResult, JsString, JsSuccess, Json, Reads, Writes}
import services.CardManager

import scala.annotation.tailrec

sealed trait Message {
  def validate(state: State): Boolean = true
  def checkPermissionError(user: User, state: State): Option[GameError] = None
  def allCardsFound(cards: List[Card], arrangement: List[BattleSlot]): Boolean = {
    arrangement match {
      case Nil => cards.isEmpty
      case bs :: remaining =>
        allCardsFound((bs.card :: bs.destroyed ::: bs.equipped).foldLeft(cards)((h, cardName) => {
          CardManager.removeOneInstanceFromCards(h, cardName)
        }), remaining)
    }
  }
}

sealed trait OwnerMessage extends Message {
  override def checkPermissionError(user: User, state: State): Option[GameError] = {
    if (user.userId != state.ownerId) {
      Some(GameError("You are not the game owner"))
    } else {
      None
    }
  }
}

sealed trait CurrentPlayerMessage extends Message {
  override def checkPermissionError(user: User, state: State): Option[GameError] = {
    state.currentStage match {
      case p : PlayerStage =>
        if (user.userId != p.currentPlayerId) {
          Some(GameError("It is not your turn"))
        } else {
          None
        }
      case _ => Some(GameError("Unexpected message"))
    }
  }
  def canBuyCards(village: Village, cards: List[String]): Boolean = {
    cards.foldLeft((village, List[String]()))((status, cardName) => {
      val (v, failures) = status
      val (newVillage, possibleCard) = v.takeCard(cardName, topOnly = true)
      if (possibleCard.isEmpty)
        (v, cardName :: failures)
      else
        (newVillage, failures)
    }) match {
      case (_, Nil) => true
      case (v, f) if f.length < cards.length => canBuyCards(v, f)
      case _ => false
    }
  }
}

case class Authentication(token: String) extends Message

object Authentication {
  implicit val authenticationFormat: Format[Authentication] = Json.format
}

case class ConnectToGame(gameId: Int) extends Message
object ConnectToGame {
  implicit val connectFormat: Format[ConnectToGame] = Json.format
}

case class GameState(state: JsObject) extends Message
object GameState {
  implicit val gameStateFormat: Format[GameState] = Json.format
}

case class GameError(message: String) extends Message
object GameError {
  implicit val errorFormat: Format[GameError] = Json.format
}

case class AcceptPlayer(userId: Int) extends OwnerMessage
object AcceptPlayer {
  implicit val acceptPlayerFormat: Format[AcceptPlayer] = Json.format
}

case class RejectPlayer(userId: Int) extends OwnerMessage
object RejectPlayer {
  implicit val rejectPlayerFormat: Format[RejectPlayer] = Json.format
}

case class AttributeResult(attributes: Attributes) extends Message
object AttributeResult {
  implicit val attributeResultFormat: Format[AttributeResult] = Json.format
}

case object GameOver extends Message

case object LeaveGame extends Message

case object JoinGame extends Message

case object StartGame extends OwnerMessage

object Message {
  implicit val messageFormat: Format[Message] = Format[Message](
    Reads { js =>
      val messageType: JsResult[String] = (JsPath \ "messageType").read[String].reads(js)
      messageType.fold(
        _ => JsError("stage undefined or incorrect"), {
          case "Authentication" => (JsPath \ "data").read[Authentication].reads(js)
          case "ConnectToGame" => (JsPath \ "data").read[ConnectToGame].reads(js)
          case "LeaveGame" => JsSuccess(LeaveGame)
          case "JoinGame" => JsSuccess(JoinGame)
          case "StartGame" => JsSuccess(StartGame)
          case "AcceptPlayer" => (JsPath \ "data").read[AcceptPlayer].reads(js)
          case "RejectPlayer" => (JsPath \ "data").read[RejectPlayer].reads(js)
          case "ChooseRest" => JsSuccess(ChooseRest)
          case "ChooseVillage" => JsSuccess(ChooseVillage)
          case "ChooseDungeon" => JsSuccess(ChooseDungeon)
          case "Destroy" => (JsPath \ "data").read[Destroy].reads(js)
          case "Purchase" => (JsPath \ "data").read[Purchase].reads(js)
          case "TakeSpoils" => (JsPath \ "data").read[TakeSpoils].reads(js)
          case "Battle" => (JsPath \ "data").read[Battle].reads(js)
          case "Upgrade" => (JsPath \ "data").read[Upgrade].reads(js)
          case "Banish" => (JsPath \ "data").read[Banish].reads(js)
          case "GetAttributes" => (JsPath \ "data").read[GetAttributes].reads(js)
          case "Discard" => (JsPath \ "data").read[Discard].reads(js)
          case "Borrow" => (JsPath \ "data").read[Borrow].reads(js)
        }
      )
    },
    Writes {
      case e: GameError =>
        JsObject(
          Seq(
            "messageType" -> JsString("Error"),
            "data" -> GameError.errorFormat.writes(e)
          )
        )
      case gs: GameState =>
        JsObject(
          Seq(
            "messageType" -> JsString("GameState"),
            "data" -> GameState.gameStateFormat.writes(gs)
          )
        )
      case ar: AttributeResult =>
        JsObject(
          Seq(
            "messageType" -> JsString("AttributeResult"),
            "data" -> AttributeResult.attributeResultFormat.writes(ar)
          )
        )
      case m =>
        JsObject(
          Seq(
            // Scala classes end up with $ tacked at the end
            "messageType" -> JsString(m.getClass.getSimpleName.replace("$", ""))
          )
        )
    }
  )
}

case object ChooseVillage extends CurrentPlayerMessage
case object ChooseRest extends CurrentPlayerMessage
case object ChooseDungeon extends CurrentPlayerMessage

case class Destroy(cardNames: Map[String, List[String]], borrowedDestroy: List[Borrowed]) extends CurrentPlayerMessage {
  def cards(state: State): List[Card] = {
    CardManager.getCardsFromHand(cardNames.values.flatten.toList, state.currentPlayer.get.userId, state)
  }
  override def validate(state: State): Boolean = {
    val cardList = cards(state)
    state.currentStage match {
      case _: Resting => cardList.length <= 1
      case d: Destroying =>
        val borrowed = d.borrowedCardsDestroyed(borrowedDestroy)._1
        cardList.length + borrowed.length == 1
      case _: Purchasing => cardList.nonEmpty
      case _ => true
    }
  }

}
object Destroy {
  implicit val destroyFormat: Format[Destroy] = Json.format
}

case class Discard(borrows: List[String], discards: List[String]) extends Message {
  def cards(playerId: Int, state: State): List[Card] = {
    CardManager.getCardsFromHand(borrows ::: discards, playerId, state)
  }
  override def validate(state: State): Boolean = {
    val cardList = cards(state.currentPlayer.get.userId, state)
    state.currentStage match {
      case d: PlayerDiscard =>
        val heroDiscardCount = cardList.collect({case _: HeroCard => true}).length
        d.howMany == borrows.length + discards.length + math.min(heroDiscardCount, d.heroesDouble)
      case _ => false
    }
  }
}
object Discard {
  implicit val discardFormat: Format[Discard] = Json.format
}

case class Borrow(borrowed: List[Int]) extends CurrentPlayerMessage {
  override def validate(state: State): Boolean = {
    state.currentStage match {
      case BorrowHeroes(_, available, canPick) =>
        // checking unique values by verifying that set has the same size as list length (toSet will dedupe)
        borrowed.toSet.size == borrowed.length && borrowed.length == canPick && borrowed.forall(i => i >= 0 && i < available.length)
      case _ => false
    }
  }
}

object Borrow {
  implicit val borrowFormat: Format[Borrow] = Json.format
}

case class Banish(dungeonOrder: List[String], destroyed: String) extends CurrentPlayerMessage {
  override def validate(state: State): Boolean = {
    @tailrec
    def validRearrange(originalNames: List[String], newNames: List[String]): Boolean = {
      def removeOne(s: String, l: List[String]): List[String] = {
        l match {
          case Nil => Nil
          case x :: xs if x == s => xs
          case x :: xs => x :: removeOne(s, xs)
        }
      }
      originalNames match {
        case Nil => true
        case x :: xs if newNames.head == x =>
          validRearrange(xs, newNames.tail)
        case x :: xs if newNames.contains(x) => validRearrange(xs, removeOne(x, newNames))
      }
    }
    @tailrec
    def validBanish(originalNames: List[String], newNames: List[String], removed: Option[String]): Boolean = {
      originalNames match {
        case Nil => removed.isEmpty
        case x :: xs if newNames.head == x => validBanish(xs, newNames.tail, removed)
        case x :: xs if removed.contains(x) => validBanish(xs, newNames, None)
        case _ => false
      }
    }
    val originalNames = state.dungeon.get.ranks.map(_.map(card => card.getName).getOrElse("CardBack"))
    val orderValid = if (dungeonOrder.drop(originalNames.length).head == "CardBack") {
      validRearrange(originalNames, dungeonOrder)
    } else {
      validBanish(originalNames, dungeonOrder, dungeonOrder.drop(originalNames.length).headOption)
    }
    orderValid && state.currentPlayer.get.hand.exists(c => c.getName == destroyed)
  }
  def cards(dungeon: Dungeon): List[Option[Card]] = {
    def findCards(names: List[String], cardsByName: Map[String, List[Card]]): List[Option[Card]] = {
        names match {
          case Nil => Nil
          case name :: remaining if cardsByName.contains(name) =>
            cardsByName(name).headOption :: findCards(remaining, cardsByName + (name -> cardsByName(name).drop(1)))
          case _ :: remaining => None :: findCards(remaining, cardsByName)
        }
    }
    val cardsByName = dungeon.ranks.flatten.groupBy(c => c.getName)
    findCards(dungeonOrder, cardsByName)
  }
}

object Banish {
  implicit val banishFormat: Format[Banish] = Json.format
}

case class TakeSpoils(bought: List[String], sentToBottom: Option[Int]) extends CurrentPlayerMessage {
  override def validate(state: State): Boolean = {
    val canBanish = state.currentStage match {
      case t: TakingSpoils => t.spoilsTypes.contains("SendToBottom")
      case _ => false
    }
    val validateBanish = sentToBottom.forall(monsterIndex => {
      state.dungeon.get.ranks.drop(monsterIndex - 1).nonEmpty &&
        canBanish
    })
    canBuyCards(state.village.get, bought) && validateBanish
  }
}

object TakeSpoils {
  implicit val takeSpoilsFormat: Format[TakeSpoils] = Json.format
}

case class Purchase(bought: List[String], destroyed: Map[String, List[String]]) extends CurrentPlayerMessage {
  override def validate(state: State): Boolean = {
    canBuyCards(state.village.get, bought) && destroyed.foldLeft(
      Right(state.currentStage.currentPlayer(state).get.hand): Either[GameError, List[Card]]
    )((possibleHand, pair) => {
      val (mainCard, otherCards) = pair
      possibleHand.fold(
        e => Left(e),
        hand => {
          val cardsToRemove = if (otherCards.contains(mainCard)) {
            otherCards
          } else {
            mainCard :: otherCards
          }
          val newHand = cardsToRemove.foldLeft(hand)((h, c) => CardManager.removeOneInstanceFromCards(h, c))
          if (newHand.length == hand.length - cardsToRemove.length) {
            Right(newHand)
          } else {
            Left(new GameError("Can't find cards in hand"))
          }
        })
    }).isRight
  }
}

object Purchase {
  implicit val purchaseFormat: Format[Purchase] = Json.format
}
case class Battle(monster: Int, arrangement: List[BattleSlot]) extends CurrentPlayerMessage {
  def validate(hand: List[Card], dungeon: Dungeon): Boolean = {
    allCardsFound(hand, arrangement) && dungeon.hasMonster(monster)
  }
}
object Battle {
  implicit val battleFormat: Format[Battle] = Json.format
}

case class GetAttributes(monster: Option[Int], arrangement: List[BattleSlot]) extends Message {
  def validate(hand: List[Card], dungeon: Dungeon): Boolean = {
    allCardsFound(hand, arrangement) && monster.forall(index => dungeon.hasMonster(index))
  }
}
object GetAttributes {
  implicit val getAttributesFormat: Format[GetAttributes] = Json.format
}

case class Upgrade(upgrades: Map[String, String]) extends CurrentPlayerMessage {
  override def validate(state: State): Boolean = {
    def findAll[T, U](starting: T, transform: (T, U) => Option[T], targets: Iterable[U]): Boolean = {
      targets.foldLeft(starting, true)((status, target) => {
        transform(status._1, target) match {
          case Some(t) => (t, status._2)
          case None => (status._1, false)
        }
      })._2
    }
    findAll[Village, String](state.village.get, (v, name) => v.takeCard(name) match {
      case (v, Some(_)) => Some(v)
      case _ => None
    }, upgrades.values) &&
      findAll[List[Card], String](state.currentPlayer.get.hand, (hand, name) =>
        CardManager.removeOneInstanceFromCards(hand, name) match {
          case newHand if newHand.length < hand.length => Some(newHand)
          case _ => None
        }, upgrades.keys
      )
  }
}

object Upgrade {
  implicit val upgradeFormat: Format[Upgrade] = Json.format
}
