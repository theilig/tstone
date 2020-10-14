export const isGeneralEffect = (effect) => {
    return effect.repeatable
}

export const isEarlyEffect = (effect) => {
    return (effect.adjustment && (effect.adjustment.operation === "Add" || effect.adjustment.operation === "Subtract"))
}

export const isLateEffect = (effect) => {
    return (!isGeneralEffect(effect) && !isEarlyEffect(effect));
}

export const executeEffect = (effect, attributes, originalCard) => {
    let newAttributes = attributes
    if (newAttributes) {
        let attributeMap = {
            "Attack": "attack",
            "Magic Attack": "magicAttack"
        }
        let affected = attributeMap[effect.adjustment.attribute]
        switch (effect.adjustment.operation) {
            case "Add":
                newAttributes[affected] = newAttributes[affected] + effect.adjustment.amount
                break
            case "Net":
                newAttributes[affected] = newAttributes[affected] + effect.adjustment.amount + originalCard.data[affected]
            default:
                break
        }
        return newAttributes;
    }
}

export const cardMatches = (card, effect, activeCard) => {
    switch (effect.requiredType) {
        case "Hero": return card.cardType === "HeroCard"
        case "Food": return card.data.traits.contains("Food")
        case "Militia": return card.data.name === "Militia"
        case "Self": return card.index === activeCard.index
        case "GoldValue": return card.data.goldValue != null
        case undefined: return true
        default: return false
    }
}

