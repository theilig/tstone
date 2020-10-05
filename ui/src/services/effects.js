export const isGeneralEffect = (effect) => {
    return effect.repeatable
}

export const isEarlyEffect = (effect) => {
    return (effect.adjustment && (effect.adjustment.operation === "Add" || effect.adjustment.operation === "Subtract"))
}

export const isLateEffect = (effect) => {
    return (!isGeneralEffect(effect) && !isEarlyEffect(effect));
}

export const executeEffect = (effect, attributes) => {
    let newAttributes = attributes
    if (newAttributes) {
        let attributeMap = {
            "ATT": "attack",
            "MATT": "magicAttack"
        }
        let affected = attributeMap[effect.adjustment.attribute]
        switch (effect.adjustment.operation) {
            case "Add":
                newAttributes[affected] = newAttributes[affected] + effect.adjustment.amount
                break
            default:
                break
        }
        return newAttributes;
    }
}

