import React, { useState } from "react";
import {useGameState} from "../context/GameState";
import cardImages from "../img/cards/cards"
import styled from "styled-components";

const DungeonContainer = styled.div`
    display: flex;
    flex-direction: row;
    margin-bottom: 20px;
`;

const DungeonCard = styled.img`
    width: 140px;
    height: 200px;
    margin-left: 10px;
`;


function Dungeon(props) {
    const { gameState } = useGameState()
    return (
        <DungeonContainer>
            {gameState.dungeon.monsterPile.map((card) => (
                <DungeonCard id={card.data.id} src={cardImages[card.data.name]} title={card.data.name} />
            ))}
            <DungeonCard id={100} src={cardImages['Disease']} title={'Disease'} />
        </DungeonContainer>
    )
}

export default Dungeon;