package models.game

abstract class Pile(cards: List[Card]) {
  def topCard: Card = cards.head
  def counts: List[Int] = List(cards.length)
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

class ItemPile(cards: List[ItemCard]) extends Pile(cards)

object ItemPile {
  def apply(card: ItemCard): ItemPile = {
    new ItemPile(List.fill(8, card))
  }
}

class SpellPile(cards: List[SpellCard]) extends Pile(cards)

object SpellPile {
  def apply(card: SpellCard): SpellPile = {
    new SpellPile(List.fill(8, card))
  }
}

class WeaponPile(cards: List[WeaponCard]) extends Pile(cards)

object WeaponPile {
  def apply(card: WeaponCard): WeaponPile = {
    new WeaponPile(List.fill(8, card))
  }
}

class VillagerPile(cards: List[VillagerCard]) extends Pile(cards)

object VillagerPile {
  def apply(card: VillagerCard): VillagerPile = {
    new VillagerPile(List.fill(8, card))
  }
}
