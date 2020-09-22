package models.game

import play.api.libs.json.{Format, Json}

import scala.collection.mutable.ListBuffer

abstract class Pile[T <: Card](cards: ListBuffer[T]) {
  def topCard: Card = cards.head
  def counts: List[Int] = List(cards.length)
  def takeTopCard: Card = {
    val card = cards.head
    cards -= card
    card
  }
}

case class DiseasePile(cards: ListBuffer[DiseaseCard]) extends Pile[DiseaseCard](cards)

object DiseasePile {
  implicit val diseasePileFormat: Format[DiseasePile] = Json.format[DiseasePile]

  def apply(card: DiseaseCard): DiseasePile = {
    new DiseasePile(ListBuffer.fill(card.frequency)(card))
  }
}

case class HeroPile(cards: ListBuffer[HeroCard]) extends Pile[HeroCard](cards) {
  override def counts: List[Int] = {
    val groups = cards.groupBy(c => c.level)
    List(
      groups.get(1).map(_.length).getOrElse(0),
      groups.get(2).map(_.length).getOrElse(0),
      groups.get(3).map(_.length).getOrElse(0)
    )
  }
}

object HeroPile {
  implicit val heroPileFormat: Format[HeroPile] = Json.format[HeroPile]
  def apply(cards: Iterable[HeroCard], isStartingCard: Boolean): HeroPile = {
    if (isStartingCard) {
      new HeroPile(ListBuffer.fill(cards.head.frequency)(cards.head))
    } else {
      val l1Card = cards.find(c => c.level == 1)
      val l2Card = cards.find(c => c.level == 2)
      val l3Card = cards.find(c => c.level == 3)
      new HeroPile(
        ListBuffer.fill(l1Card.get.frequency)(l1Card.get) ++
          ListBuffer.fill(l2Card.get.frequency)(l2Card.get) ++
          ListBuffer.fill(l3Card.get.frequency)(l3Card.get)
      )
    }
  }
}

case class ItemPile(cards: ListBuffer[ItemCard]) extends Pile(cards)

object ItemPile {
  implicit val itemPileFormat: Format[ItemPile] = Json.format[ItemPile]

  def apply(card: ItemCard, isStartingCard: Boolean): ItemPile = {
    new ItemPile(ListBuffer.fill(card.frequency)(card))
  }
}

case class SpellPile(cards: ListBuffer[SpellCard]) extends Pile(cards)

object SpellPile {
  implicit val spellPileFormat: Format[SpellPile] = Json.format[SpellPile]
  def apply(card: SpellCard): SpellPile = {
    new SpellPile(ListBuffer.fill(card.frequency)(card))
  }
}

case class WeaponPile(cards: ListBuffer[WeaponCard]) extends Pile(cards)

object WeaponPile {
  implicit val weaponPileFormat: Format[WeaponPile] = Json.format[WeaponPile]
  def apply(card: WeaponCard, isStartingCard: Boolean): WeaponPile = {
    new WeaponPile(ListBuffer.fill(card.frequency)(card))
  }
}

case class VillagerPile(cards: ListBuffer[VillagerCard]) extends Pile(cards)

object VillagerPile {
  implicit val villagerPileFormat: Format[VillagerPile] = Json.format[VillagerPile]
  def apply(card: VillagerCard): VillagerPile = {
    new VillagerPile(ListBuffer.fill(card.frequency)(card))
  }
}
