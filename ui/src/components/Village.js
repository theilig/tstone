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

function Village(props) {
    const { gameState } = useGameState()
    const purchased = props.purchased ?? []
    return (
        <VillageContainer>
            <VillageRow key={1}>
                {gameState.village.heroes.map((pile, index) => (
                    <VillagePile key={index + 10} pile={pile}
                                 registerHovered={props.registerHovered}
                                 registerDrop={props.registerDrop}
                                 purchased={purchased.filter((r) => {
                                     return r.key === "heroes" && r.index === index
                                 })}
                    />
                ))}
                {gameState.village.weapons.map((pile,index) => (
                    <VillagePile key={index + 20} pile={pile} registerHovered={props.registerHovered}
                                 registerDrop={props.registerDrop}
                                 purchased={purchased.filter((r) => {
                                     return r.key === "weapons" && r.pile === index
                                 })}
                    />
                ))}
            </VillageRow>
            <VillageRow key={2}>
                {gameState.village.spells.map((pile, index) => (
                    <VillagePile key={index + 30} pile={pile} registerHovered={props.registerHovered}
                                 registerDrop={props.registerDrop}
                                 purchased={purchased.filter((r) => {
                                     return r.key === "spells" && r.pile === index
                                 })}
                    />
                ))}
                {gameState.village.items.map((pile, index) => (
                    <VillagePile key={index + 40} pile={pile} registerHovered={props.registerHovered}
                                 registerDrop={props.registerDrop}
                                 purchased={purchased.filter((r) => {
                                     return r.key === "items" && r.index === index
                                 })}
                    />
                ))}
                {gameState.village.villagers.map((pile, index) => (
                    <VillagePile key={index + 50} pile={pile} registerHovered={props.registerHovered}
                                 registerDrop={props.registerDrop}
                                 purchased={purchased.filter((r) => {
                                     return r.key === "villagers" && r.index === index
                                 })}
                    />
                ))}
            </VillageRow>
        </VillageContainer>
    )
}

export default Village;