export const CardTypes = {
    HERO: 'Hero',
    WEAPON: 'Weapon',
    SPELL: 'Spell',
    VILLAGER: 'Villager',
    MONSTER: 'Monster',
    FOOD: 'Food',
    ITEM: 'Item'
}

export const getDragType = (card) => {
    switch (card.cardType) {
        case "HeroCard":
            return CardTypes.HERO
        case "WeaponCard":
            return CardTypes.WEAPON
        case "SpellType":
            return CardTypes.SPELL
        case "VillagerCard":
            return CardTypes.VILLAGER
        case "MonsterCard":
            return CardTypes.MONSTER
        case "ItemCard":
            if (card.data.traits.includes("Food")) {
                return CardTypes.FOOD
            } else {
                return CardTypes.ITEM
            }
    }
}
export const getDropTypes = (card) => {
    switch (card.cardType) {
        case "HeroCard":
            return [CardTypes.FOOD, CardTypes.WEAPON]
            default:
                return [CardTypes.FOOD, CardTypes.WEAPON, CardTypes.ITEM]
    }
}