import React, {useRef} from "react";
import {cardMatches, executeEffect, isActive, isEarlyEffect} from "../services/effects";
import cardImages from "../img/cards/cards";
import {useDrag} from "react-dnd";
import {getDragType} from "./CardTypes";

export const upgradeCost = (card) => {
    if (card.data.level == null || card.data.level >= 3) {
        return 0
    }
    if (card.data.level === 1) {
        return 2
    }
    return 3
}

const initialAttributes = (card, attributes) => {
    let starting = {...attributes}
    Object.keys(card.data).forEach((key) => {
        let value = card.data[key]
        if (key in starting) {
            starting[key] = starting[key] + value
        }
    })
    return starting
}

export const getAttributes = (slots, generalEffects, isVillage) => {
    let attributes = {
        goldValue: 0,
        light: 0,
        attack: 0,
        magicAttack: 0,
        strength: 0,
        weight: 0,
        buys: 0,
        experience: 0
    }

    const activeCard = slots.cards[0]
    const selfDestroyed = slots.destroy && slots.destroy.filter(c => {
        return c.data.sourceIndex === activeCard.data.sourceIndex
    }).length > 0
    if (activeCard && activeCard.cardType !== "WeaponCard") {
        slots.cards.forEach((attachedCard) => {
            if (!selfDestroyed) {
                attributes = initialAttributes(attachedCard, attributes)
            }
            attributes = addInitialEffects(attachedCard, generalEffects, attributes)
        })

        if (slots.destroy) {
            slots.destroy.forEach((destroyedCard) => {
                if (isVillage) {
                    attributes = addDestroyEffects(
                        destroyedCard,
                        activeCard,
                        activeCard.data.villageEffects,
                        attributes
                    )
                    // handle self destroys for equipped cards
                    if (destroyedCard.name !== activeCard.name) {
                        attributes = addDestroyEffects(
                            destroyedCard,
                            destroyedCard,
                            destroyedCard.data.villageEffects,
                            attributes
                        )
                    }
                } else {
                    attributes = addDestroyEffects(
                        destroyedCard,
                        activeCard,
                        activeCard.data.dungeonEffects,
                        attributes
                    )
                    attributes = addDestroyEffects(
                        destroyedCard,
                        destroyedCard,
                        destroyedCard.data.dungeonEffects,
                        attributes
                    )
                }
            })
        }
    } else if (activeCard) {
        attributes = {goldValue: activeCard.data.goldValue}
    }
    return attributes
}

const addInitialEffects = (card, generalEffects, currentAttributes) => {
    let newCardAttributes = currentAttributes
    generalEffects.forEach((effect) => {
        if (isActive(effect, {card: card})) {
            newCardAttributes = executeEffect(effect, newCardAttributes)
        }
    })
    if (card.data.dungeonEffects) {
        card.data.dungeonEffects.forEach((effect) => {
            if (isEarlyEffect(effect) && isActive(effect, {card: card})) {
                newCardAttributes = executeEffect(effect, newCardAttributes)
            }
        })
    }
    if (card.data.villageEffects) {
        card.data.villageEffects.forEach((effect) => {
            if (isEarlyEffect(effect) && isActive(effect, {card: card})) {
                newCardAttributes = executeEffect(effect, newCardAttributes)
            }
        })
    }
    return newCardAttributes
}

const addDestroyEffects = (destroyedCard, activeCard, effects, currentAttributes) => {
    let newAttributes = {...currentAttributes}
    if (effects) {
        effects.forEach((effect) => {
            if (effect.effect === "Destroy" &&
                cardMatches(destroyedCard, effect, activeCard)) {
                newAttributes = executeEffect(effect, newAttributes, destroyedCard)
            }
        })
    }
    return newAttributes
}

export function HandCard(props) {
    const [,drag, preview] = useDrag({
        item: {type: getDragType(props.card), card: props.card},
        end: (item, monitor) => {
            if (!monitor.didDrop() && props.registerDrop) {
                props.registerDrop(props.card, null)
            }
        }
    })

    const refContainer = useRef(null)

    const handleHovered = (shift) => {
        if (refContainer && refContainer.current) {
            let location = refContainer.current.getBoundingClientRect()
            let top = location.top
            if (shift) {
                top = location.bottom - 375
            }
            props.registerHovered(props.card.data.name, {left: location.left, top: top})
        }
    }

    let marginTop = '-140px'
    if (props.small) {
        marginTop = '-78px'
    }
    if (props.position === 0) {
        marginTop = '0px'
    }

    let style = {width: '126px', height: '180px', marginLeft: '10px', marginTop:marginTop}

    if (props.small) {
        style = {width: '70px', height: '100px', marginLeft: '10px', marginTop:marginTop}
    }

    return <div ref={drag}>
        <img style={style}
              key={props.card.data.sourceIndex} id={props.card.data.sourceIndex}
              src={cardImages[props.card.data.name]} title={props.card.data.name} alt={props.card.data.name}
              ref={refContainer}
              onMouseOver={() => handleHovered(props.shiftHovered)}
              onMouseDown={() => props.registerHovered(null, null)}
              onMouseOut={() => props.registerHovered(null, null)}
        />

    </div>

}




