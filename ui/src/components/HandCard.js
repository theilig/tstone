import React from "react";
import styled from "styled-components";
import HeroCard from "./HeroCard";
import FoodCard from "./FoodCard";
import ItemCard from "./ItemCard";
import SpellCard from "./SpellCard";
import VillagerCard from "./VillagerCard";
import WeaponCard from "./WeaponCard";
import MonsterCard from "./MonsterCard";

export const HandCard = styled.img`
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

export const getCardInfo = (card, index, registerHovered) => {
    let attributes = initialAttributes(card)
    let cardElement = null
    switch (card.cardType) {
        case "HeroCard":
            cardElement = (<HeroCard key={index + 1} index={index} registerHovered={registerHovered} card={card}/>)
            break;
        case "ItemCard":
            if (card.data.traits.includes("Food")) {
                cardElement = (<FoodCard key={index + 1} index={index} registerHovered={registerHovered} card={card}/>)
            } else {
                cardElement = (<ItemCard key={index + 1} index={index} registerHovered={registerHovered} card={card}/>)
            }
            break;
        case "SpellCard":
            cardElement = (<SpellCard key={index + 1} index={index} registerHovered={registerHovered} card={card}/>)
            break;
        case "VillagerCard":
            cardElement = (<VillagerCard key={index + 1} index={index} registerHovered={registerHovered} card={card}/>)
            break
        case "WeaponCard":
            cardElement = (<WeaponCard key={index + 1} index={index} registerHovered={registerHovered} card={card}/>)
            break;
        case "MonsterCard":
            cardElement = (<MonsterCard key={index + 1} index={index} registerHovered={registerHovered} card={card}/>)
    }
    return {
        cardElement: cardElement,
        attributes: attributes
    }
}


