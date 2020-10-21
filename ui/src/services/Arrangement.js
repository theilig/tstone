import {cardMatches} from "./effects";

export const getLowerMapFromArrangement = (arrangement, lowerField) => {
    const mapData = {}
    arrangement.forEach(column => {
        const activeName = column.cards[0].data.name
        if (column[lowerField] != null) {
            column[lowerField].forEach(card => {
                let newData = mapData[activeName] ?? []
                newData.push(card.data.name)
                mapData[activeName] = newData
            })
        }
    })
    return mapData;
}

export const getCardDestroysFromArrangement = (arrangement) => {
    const mapData = {}
    arrangement.forEach(column => {
        const activeCard = column.cards[0].data
        if (column.destroy != null) {
            let effectGroups = ['villageEffects', 'dungeonEffects']
            effectGroups.forEach(group => {
                (activeCard[group] ?? []).forEach(e => {
                    if (e.adjustment &&
                        e.adjustment.operation &&
                        e.effect === 'Destroy' &&
                        e.adjustment.attribute === 'Card') {
                        column.destroy.forEach(destroyedCard => {
                            if (cardMatches(destroyedCard, e, column.cards[0])) {
                                let data = mapData[activeCard.name] ?? []
                                data.push(destroyedCard.data.name)
                                mapData[activeCard.name] = data
                            }
                        })
                    }
                })
            })
        }
    })
    return mapData;
}

export const destroyForCards = (arrangement) => {
    return Object.keys(getCardDestroysFromArrangement(arrangement)).length > 0
}