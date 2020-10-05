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
    return (
        <VillageContainer>
            <VillageRow key={1}>
                {gameState.village.heroes.map((pile, index) => (
                    <VillagePile key={index + 10} pile={pile} registerHovered={props.registerHovered} />
                ))}
                {gameState.village.weapons.map((pile,index) => (
                    <VillagePile key={index + 20} pile={pile} registerHovered={props.registerHovered} />
                ))}
            </VillageRow>
            <VillageRow key={2}>
                {gameState.village.spells.map((pile, index) => (
                    <VillagePile key={index + 30} pile={pile} registerHovered={props.registerHovered} />
                ))}
                {gameState.village.items.map((pile, index) => (
                    <VillagePile key={index + 40} pile={pile} registerHovered={props.registerHovered} />
                ))}
                {gameState.village.villagers.map((pile, index) => (
                    <VillagePile key={index + 50} pile={pile} registerHovered={props.registerHovered} />
                ))}
            </VillageRow>
        </VillageContainer>
    )
}

export default Village;