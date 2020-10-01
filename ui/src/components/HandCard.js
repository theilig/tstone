import React, {useRef} from "react";
import styled from "styled-components";
import {executeEffect, isEarlyEffect} from "../services/effects";
import cardImages from "../img/cards/cards";
import {useDrag} from "react-dnd";
import {getDragType} from "./CardTypes";

const Card = styled.img`
    width: 126px;
    height: 180px;
    margin-left: 10px;
`;

export const HoveredCard = styled.img`
    width: 200px;
    height: 285px;
`;

const initialAttributes = (card) => {
    let starting = {
        goldValue: 0,
        light: 0,
        attack: 0,
        magicAttack: 0,
        strength: 0,
        weight: 0,
    }
    Object.keys(card.data).forEach((key) => {
        let value = card.data[key]
        if (key in starting) {
            starting[key] = value
        }
    })
    return starting
}

export const getAttributes = (card, attached, generalEffects) => {
    let attributes = {}
    if (card.cardType !== "WeaponCard") {
        attributes = initialAttributes(card)
        attributes = addInitialEffects(card, generalEffects, attributes)
        if (attached) {
            attached.forEach((attachedCard) => {
                attributes = addInitialEffects(attachedCard, generalEffects, attributes)
            })
        }
    } else {
        attributes = {goldValue: card.data.goldValue}
    }
    return attributes
}

const addInitialEffects = (card, generalEffects, currentAttributes) => {
    let newCardAttributes = currentAttributes
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
    return newCardAttributes
}

export function HandCard(props) {
    const [{isDragging}, drag, preview] = useDrag({
        item: {type: props.cardType, index: props.index},
        begin: (_) => console.warn("Drop begins")
    })

    const refContainer = useRef(null)

    const handleHovered = () => {
        if (refContainer && refContainer.current) {
            props.registerHovered(props.name, refContainer.current.getBoundingClientRect())
        }
    }

    return <div ref={drag}>
        <Card key={props.id} id={props.id}
              src={cardImages[props.name]} title={props.name} alt={props.name}
              ref={refContainer}
              onMouseOver={() => handleHovered()}
              onMouseDown={() => props.registerHovered(null, null)}
              onMouseOut={() => props.registerHovered(null, null)}
        />

    </div>

}




