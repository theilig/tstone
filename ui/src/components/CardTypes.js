export const CardTypes = {
    HERO: 'Hero',
    WEAPON: 'Weapon',
    SPELL: 'Spell',
    VILLAGER: 'Villager',
    MONSTER: 'Monster',
    FOOD: 'Food',
    ITEM: 'Item',
    DISCARD: 'Discard',
    DESTROY: 'Destroy',
    VILLAGE: 'Village',
    DUNGEON: 'Dungeon',
    DISEASE: 'Disease'
}

export const getDragType = (card) => {
    switch (card.cardType) {
        case "HeroCard":
            return CardTypes.HERO
        case "WeaponCard":
            return CardTypes.WEAPON
        case "SpellCard":
            return CardTypes.SPELL
        case "VillagerCard":
            return CardTypes.VILLAGER
        case "MonsterCard":
            return CardTypes.MONSTER
        case "DiseaseCard":
            return CardTypes.DISEASE
        case "ItemCard":
            if (card.data.traits.includes("Food")) {
                return CardTypes.FOOD
            } else {
                return CardTypes.ITEM
            }
        default:
            return CardTypes.DISCARD
    }
}
export const getDropTypes = (card) => {
    switch (card.cardType) {
        case "HeroCard":
            return [CardTypes.FOOD, CardTypes.WEAPON]
        case "TakeAny":
            return [
                CardTypes.HERO, CardTypes.ITEM, CardTypes.WEAPON, CardTypes.FOOD, CardTypes.MONSTER,
                CardTypes.VILLAGER, CardTypes.SPELL, CardTypes.DISEASE]
        case "Upgrade":
            return [CardTypes.VILLAGE]
        case "Dungeon":
            return [CardTypes.MONSTER]
        default:
                return []
    }
}