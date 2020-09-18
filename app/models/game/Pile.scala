package models.game

abstract class Pile(var cards: List[Card]) {
  def topCard: Card = cards.head
  def counts: List[Int] = List(cards.length)
  def takeTopCard: Card = {
    val card = topCard
    cards = cards.tail
    card
  }
}

class HeroPile(cards: List[HeroCard]) extends Pile(cards) {
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
  def apply(cards: Iterable[HeroCard], isStartingCard: Boolean): HeroPile = {
    if (isStartingCard) {
      new HeroPile(List.fill(30)(cards.head))
    } else {
      val l1Card = cards.find(c => c.level == 1)
      val l2Card = cards.find(c => c.level == 2)
      val l3Card = cards.find(c => c.level == 3)
      new HeroPile(
        List.fill(6)(l1Card.get) ::: List.fill(4)(l2Card.get) ::: List.fill(2)(l3Card.get)
      )
    }
  }
}

class ItemPile(cards: List[ItemCard]) extends Pile(cards)

object ItemPile {
  def apply(card: ItemCard, isStartingCard: Boolean): ItemPile = {
    val frequency = if (isStartingCard) {15} else {8}
    new ItemPile(List.fill(frequency)(card))
  }
}

class SpellPile(cards: List[SpellCard]) extends Pile(cards)

object SpellPile {
  def apply(card: SpellCard): SpellPile = {
    new SpellPile(List.fill(8)(card))
  }
}

class WeaponPile(cards: List[WeaponCard]) extends Pile(cards)

object WeaponPile {
  def apply(card: WeaponCard, isStartingCard: Boolean): WeaponPile = {
    val frequency = if (isStartingCard) {15} else {8}
    new WeaponPile(List.fill(frequency)(card))
  }
}

class VillagerPile(cards: List[VillagerCard]) extends Pile(cards)

object VillagerPile {
  def apply(card: VillagerCard): VillagerPile = {
    new VillagerPile(List.fill(8)(card))
  }
}
