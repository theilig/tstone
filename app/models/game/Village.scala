package models.game

import dao.CardDao

class Village(
               heroes: List[HeroPile],
               weapons: List[WeaponPile],
               items: List[ItemPile],
               spells: List[SpellPile],
               villagers: List[VillagerPile]
             )

object Village {
  def apply(cards: List[Card], cardDao: CardDao): Village = {
    val heroesByType = cards.groupBy({case h: HeroCard => h.heroType})

    new Village(
      cards.collect({case h => HeroPile(h1, h2, h3)}),
      cards.collect({case w: WeaponCard => WeaponPile(w)}),
      cards.collect({case i: ItemCard => ItemPile(i)}),
      cards.collect({case s: SpellCard => SpellPile(s)}),
      cards.collect({case v: VillagerCard => VillagerPile(v)})
    )
  }
}
