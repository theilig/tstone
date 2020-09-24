package models.game

import play.api.libs.json.{Format, Json}

abstract class Pile[T <: Card](cards: List[T]) {
  def topCard: Card = cards.head
  def counts: List[Int] = List(cards.length)
}

case class DiseasePile(cards: List[DiseaseCard]) extends Pile[DiseaseCard](cards)

object DiseasePile {
  implicit val diseasePileFormat: Format[DiseasePile] = Json.format[DiseasePile]

  def apply(card: DiseaseCard): DiseasePile = {
    new DiseasePile(List.fill(card.frequency)(card))
  }
}

case class HeroPile(cards: List[HeroCard]) extends Pile[HeroCard](cards) {
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
  def apply(cards: Iterable[HeroCard]): HeroPile = {
    val orderedCards: List[HeroCard] = cards.toList.sortBy(_.level)
    val pileCards = orderedCards.foldLeft(List[HeroCard]())((l, card) =>
      l ++ List.fill(card.frequency)(card))
    new HeroPile(pileCards)
  }
}

case class ItemPile(cards: List[ItemCard]) extends Pile(cards)

object ItemPile {
  implicit val itemPileFormat: Format[ItemPile] = Json.format[ItemPile]

  def apply(card: ItemCard): ItemPile = {
    new ItemPile(List.fill(card.frequency)(card))
  }
}

case class SpellPile(cards: List[SpellCard]) extends Pile(cards)

object SpellPile {
  implicit val spellPileFormat: Format[SpellPile] = Json.format[SpellPile]
  def apply(card: SpellCard): SpellPile = {
    new SpellPile(List.fill(card.frequency)(card))
  }
}

case class WeaponPile(cards: List[WeaponCard]) extends Pile(cards)

object WeaponPile {
  implicit val weaponPileFormat: Format[WeaponPile] = Json.format[WeaponPile]
  def apply(card: WeaponCard): WeaponPile = {
    new WeaponPile(List.fill(card.frequency)(card))
  }
}

case class VillagerPile(cards: List[VillagerCard]) extends Pile(cards)

object VillagerPile {
  implicit val villagerPileFormat: Format[VillagerPile] = Json.format[VillagerPile]
  def apply(card: VillagerCard): VillagerPile = {
    new VillagerPile(List.fill(card.frequency)(card))
  }
}
