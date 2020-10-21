package models.game

import controllers.game.stage.{Destroying, PlayerStage, Purchasing, Resting, TakingSpoils}
import models.User
import play.api.libs.json.{Format, JsError, JsObject, JsPath, JsResult, JsString, JsSuccess, Json, Reads, Writes}
import services.CardManager

import scala.annotation.tailrec

sealed trait Message {
  def validate(state: State): Boolean = true
  def checkPermissionError(user: User, state: State): Option[GameError] = None
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
}

case class Authentication(token: String) extends Message

object Authentication {
  implicit val authenticationFormat: Format[Authentication] = Json.format
}

case class ConnectToGame(gameId: Int) extends Message
object ConnectToGame {
  implicit val connectFormat: Format[ConnectToGame] = Json.format
}

case class GameState(state: State) extends Message
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
          case "Battle" => (JsPath \ "data").read[Battle].reads(js)
          case "Upgrade" => (JsPath \ "data").read[Upgrade].reads(js)
          case "Banish" => (JsPath \ "data").read[Banish].reads(js)
          case "Loan" => (JsPath \ "data").read[Loan].reads(js)
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

case class Destroy(cardNames: Map[String, List[String]]) extends CurrentPlayerMessage {
  override def validate(state: State): Boolean = {
    val cardList = cards(state)
    state.currentStage match {
      case _: Resting => cardList.length <= 1
      case d: Destroying => cardList.length != 1
      case _: Purchasing => cardList.nonEmpty
      case _ => true
    }
  }

  def cards(state: State): List[Card] = {
    cardNames.values.flatten.foldLeft(state.currentPlayer.get.hand, List[Card]())((soFar, name) => {
      val (hand, alreadyFound) = soFar
      hand.find(_.getName == name).map(c =>
        (CardManager.removeOneInstanceFromCards(hand, name), c :: alreadyFound)
      ).getOrElse((hand, alreadyFound))
    })._2
  }
}
object Destroy {
  implicit val destroyFormat: Format[Destroy] = Json.format
}

case class Banish(banished: List[String]) extends CurrentPlayerMessage {
  override def validate(state: State): Boolean = {
    val dungeon = state.dungeon.get.monsterPile.take(3)
    val remaining = banished.foldLeft(dungeon)((d, name) => {
      CardManager.removeOneInstanceFromCards(d, name)
    })
    remaining.length + banished.length == dungeon.length
  }
}

object Banish {
  implicit val banishFormat: Format[Banish] = Json.format
}

case class Purchase(bought: List[String], destroyed: Map[String, List[String]]) extends CurrentPlayerMessage {
  override def validate(state: State): Boolean = {
  @tailrec
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
    val correctDestroys = state.currentStage match {
      case _: Purchasing => true
      case _: TakingSpoils => destroyed.isEmpty
    }
    correctDestroys && canBuyCards(state.village.get, bought) && destroyed.foldLeft(
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
    @tailrec
    def allCardsFound(cards: List[Card], arrangement: List[BattleSlot]): Boolean = {
      arrangement match {
        case Nil => cards.isEmpty
        case bs :: remaining =>
          allCardsFound((bs.card :: bs.destroyed ::: bs.equipped).foldLeft(cards)((h, cardName) => {
            CardManager.removeOneInstanceFromCards(h, cardName)
          }), remaining)
      }
    }
    allCardsFound(hand, arrangement) && (dungeon.monsterPile.drop(monster - 1).head match {
      case _: MonsterCard => true
      case _ => false
    })
  }
}
object Battle {
  implicit val battleFormat: Format[Battle] = Json.format
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

case class Loan(hero: String) extends Message

object Loan {
  implicit val loanFormat: Format[Loan] = Json.format
}
