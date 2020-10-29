package models.game

import models.User
import play.api.libs.json.{Json, OFormat}
import models.schema.Tables.UserRow
import services.AttributeCalculator

import scala.annotation.tailrec
import scala.util.Random

case class Player(userId: Int,
                  name: String,
                  pending: Boolean,
                  discard: List[Card],
                  hand: List[Card],
                  deck: List[Card],
                  xp: Int
                 ) {
  def finishTurn: Player = {
    @tailrec
    def buildNewHand(discard: List[Card], deck: List[Card], hand: List[Card]): (List[Card], List[Card], List[Card]) = {
      val cardsNeeded = Player.HandSize - hand.length
      cardsNeeded match {
        case 0 => (discard, deck, hand)
        case x if deck.length >= x => (discard, deck.drop(x), hand ::: deck.take(x))
        case _ if discard.isEmpty => (discard, Nil, hand ::: deck)
        case _ =>
          val random = new Random
          buildNewHand(Nil, random.shuffle(discard), hand ::: deck)
      }
    }
    val (newDiscard, newDeck, newHand) = buildNewHand(discard, deck, hand)
    copy(discard = newDiscard, hand = newHand, deck = newDeck)
  }
  def attributes: Attributes = AttributeCalculator.getValues(this, hand.map(
    c => BattleSlot(c.getName, Nil, Nil, Some(hand))
  ), None).attributes
}

object Player {
  implicit val playerFormat: OFormat[Player] = Json.format[Player]

  val HandSize = 6

  def apply(userId: Int, firstName: String, lastName: String, pending: Boolean): Player = {
    new Player(userId, s"$firstName ${lastName.head}.", pending, Nil, Nil, Nil, 0)
  }
  def apply(userRow: UserRow, pending: Boolean): Player = {
    apply(userRow.userId, userRow.firstName, userRow.lastName, pending)
  }

  def apply(user: User, pending: Boolean): Player = {
    apply(user.userId, user.firstName, user.lastName, pending)
  }
}
