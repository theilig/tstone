import React, { useState } from "react";
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
            <VillageRow>
                {gameState.village.heroes.map((pile) => (
                    <VillagePile pile={pile} registerHovered={props.registerHovered} />
                ))}
                {gameState.village.weapons.map((pile) => (
                    <VillagePile pile={pile} registerHovered={props.registerHovered} />
                ))}
            </VillageRow>
            <VillageRow>
                {gameState.village.spells.map((pile) => (
                    <VillagePile pile={pile} registerHovered={props.registerHovered} />
                ))}
                {gameState.village.items.map((pile) => (
                    <VillagePile pile={pile} registerHovered={props.registerHovered} />
                ))}
                {gameState.village.villagers.map((pile) => (
                    <VillagePile pile={pile} registerHovered={props.registerHovered} />
                ))}
            </VillageRow>
        </VillageContainer>
    )
}

export default Village;