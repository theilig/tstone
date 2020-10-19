export const isGeneralEffect = (effect) => {
    return effect.repeatable
}

export const isEarlyEffect = (effect) => {
    return (effect.adjustment && (
        effect.adjustment.operation === "Add" ||
        effect.adjustment.operation === "Subtract" ||
        effect.adjustment.operation === "Net"
    ))
}

export const isLateEffect = (effect) => {
    return (!isGeneralEffect(effect) && !isEarlyEffect(effect));
}

export const isActive = (effect, data) => {
    if (effect.requiredType != null) {
        return cardMatches(data.card, effect, null)
    } else {
        return true
    }
}

export const executeEffect = (effect, attributes, originalCard) => {
    let newAttributes = {...attributes}
    if (newAttributes) {
        let attributeMap = {
            "Attack": "attack",
            "Magic Attack": "magicAttack",
            "Gold": "goldValue",
            "Experience": "experience",
            "Buys": "buys"
        }
        let affected = attributeMap[effect.adjustment.attribute]
        switch (effect.adjustment.operation) {
            case "Add":
                newAttributes[affected] = newAttributes[affected] + effect.adjustment.amount
                break
            case "Net":
                newAttributes[affected] = newAttributes[affected] + effect.adjustment.amount + originalCard.data[affected]
                break
            default:
                break
        }
        return newAttributes;
    }
}

export const cardMatches = (card, effect, activeCard) => {
    if (card.data == null) {
        card = {data: card}
    }
    switch (effect.requiredType) {
        case "Hero": return card.cardType === "HeroCard"
        case "Food": return card.data.traits.contains("Food")
        case "Militia": return card.data.name === "Militia"
        case "Self": return activeCard && card.data.sourceIndex === activeCard.data.sourceIndex
        case "GoldValue": return card.data.goldValue != null
        case undefined: return true
        default: return false
    }
}

