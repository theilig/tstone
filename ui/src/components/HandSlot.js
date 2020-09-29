import React, {useState, useEffect, useRef, createRef} from "react";
import cardImages from "../img/cards/cards";
import {HandCard} from "./HandCard";
import {executeEffect, isEarlyEffect} from "../services/effects";
function HandSlot(props) {
    const [cardRefs, setCardRefs] = useState([]);
    const {attached, generalEffects, card, index, setAttributes} = props
    useEffect(() => {
        setCardRefs(cardRefs => (
                Array(attached.length + 1).fill().map((_, i) => cardRefs[i] || createRef())
            ))
        let newAttributes = {}
        if (card.cardType !== "WeaponCard") {
            newAttributes = addInitialEffects(card, index, generalEffects, newAttributes)
            attached.forEach((attachedCard) => {
                newAttributes = addInitialEffects(attachedCard, index, generalEffects, newAttributes)
            })
        }
        setAttributes(newAttributes, index)
    }, [attached, generalEffects, card, index, setAttributes]);

    const handleHovered = (index) => {
        if (cardRefs[index] && cardRefs[index].current) {
            let card = props.card
            if (index > 0) {
                card = props.attached[index - 1]
            }
            props.registerHovered(card.data, cardRefs[index].current.getBoundingClientRect())
        }
    }

    const addInitialEffects = (card, index, generalEffects, currentAttributes) => {
        let newCardAttributes = currentAttributes[index]
        generalEffects.forEach((effect) => {
            newCardAttributes = executeEffect(effect, newCardAttributes)
        })
        if (card.data.dungeonEffects) {
            card.data.dungeonEffects.forEach((effect) => {
                if (isEarlyEffect(effect)) {
                    newCardAttributes = executeEffect(effect, newCardAttributes)
                }
            })
        }
        if (card.data.villageEffects) {
            card.data.villageEffects.forEach((effect) => {
                if (isEarlyEffect(effect)) {
                    newCardAttributes = executeEffect(effect, newCardAttributes)
                }
            })
        }
        let newAttributes = currentAttributes
        newAttributes[index] = newCardAttributes
        return newAttributes
    }


    let allCards = [props.card, ...props.attached]
    return (
        <div>
            {allCards.map((c, index) => (
                <HandCard key={c.data.id + props.index * 200} id={c.data.id + props.index * 200}
                          src={cardImages[c.data.name]} title={c.data.name}
                          onMouseOver={() => handleHovered(index)}
                          ref={cardRefs[index]}
                          onMouseOut={() => props.registerHovered(null, null)}
                              style={{
                                  position: 'relative',
                                  top: index * 20 + 'px',
                                  left: 0,
                              }}
                />))
            }
        </div>
    )
}

export default HandSlot;