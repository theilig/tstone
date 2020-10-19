import {cardMatches} from "./effects";

export const getLowerMapFromArrangement = (arrangement) => {
    const mapData = {}
    arrangement.forEach(column => {
        const activeName = column[0][0].data.name
        if (column[1] != null) {
            column[1].forEach(card => {
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
        const activeCard = column[0][0].data
        if (column[1] != null) {
            let effectGroups = ['villageEffects', 'dungeonEffects']
            effectGroups.forEach(group => {
                (activeCard[group] ?? []).forEach(e => {
                    if (e.adjustment &&
                        e.adjustment.operation &&
                        e.effect === 'Destroy' &&
                        e.adjustment.attribute === 'Card') {
                        column[1].forEach(destroyedCard => {
                            if (cardMatches(destroyedCard, e, column[0][0])) {
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