import React from "react";
import styled from "styled-components";
import HeroCard from "./HeroCard";
import FoodCard from "./FoodCard";
import ItemCard from "./ItemCard";
import SpellCard from "./SpellCard";
import VillagerCard from "./VillagerCard";
import WeaponCard from "./WeaponCard";
import MonsterCard from "./MonsterCard";
import {executeEffect, isEarlyEffect} from "../services/effects";

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



