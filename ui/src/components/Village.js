import React from "react";
import {useGameState} from "../context/GameState";
import styled from "styled-components";
import VillagePile from "./VillagePile";

const VillageContainer = styled.div`
    display: flex;
    flex-direction: column;
    margin-bottom: 20px;
`;
const VillageRow = styled.div`
    display: flex;
    flex-direction: row;
    margin-bottom: 20px;
`;

export const villageCategories = ['heroes', 'weapons', 'spells', 'items', 'villagers']

function Village(props) {
    const { gameState } = useGameState()
    const purchased = props.purchased ?? []
    return (
        <VillageContainer>
            <VillageRow key={1}>
                {gameState.village.heroes.map((pile, index) => {
                    const cards = pile.cards.map(c => {
                        return {cardType: 'HeroCard', data: c}
                    })
                    return (
                        <VillagePile key={index + 10} pile={cards}
                                     registerHovered={props.registerHovered}
                                     registerDrop={props.registerDrop}
                                     upgrading={props.upgrading}
                                     purchased={purchased}
                        />
                    )
                })}
                {gameState.village.weapons.map((pile,index) => {
                    const cards = pile.cards.map(c => {
                        return {cardType: 'WeaponCard', data: c}
                    })
                    return (
                        <VillagePile key={index + 20} pile={cards} registerHovered={props.registerHovered}
                                     registerDrop={props.registerDrop}
                                     purchased={purchased}
                        />

                    )
                })}
            </VillageRow>
            <VillageRow key={2}>
                {gameState.village.spells.map((pile, index) => {
                    const cards = pile.cards.map(c => {
                        return {cardType: 'SpellCard', data: c}
                    })
                    return (
                        <VillagePile key={index + 30} pile={cards} registerHovered={props.registerHovered}
                                     registerDrop={props.registerDrop}
                                     purchased={purchased}
                        />
                    )
                })}
                {gameState.village.items.map((pile, index) => {
                    const cards = pile.cards.map(c => {
                        return {cardType: 'ItemCard', data: c}
                    })
                    return (
                        <VillagePile key={index + 40} pile={cards} registerHovered={props.registerHovered}
                                     registerDrop={props.registerDrop}
                                     purchased={purchased}
                        />
                    )
                })}
                {gameState.village.villagers.map((pile, index) => {
                    const cards = pile.cards.map(c => {
                        return {cardType: 'VillagerCard', data: c}
                    })
                    return (
                        <VillagePile key={index + 50} pile={cards} registerHovered={props.registerHovered}
                                     registerDrop={props.registerDrop}
                                     purchased={purchased}
                        />
                    )
                })}
            </VillageRow>
        </VillageContainer>
    )
}

export default Village;