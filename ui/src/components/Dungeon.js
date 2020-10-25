import React from "react";
import {useGameState} from "../context/GameState";
import cardImages from "../img/cards/cards"
import styled from "styled-components";
import DungeonSlot from "./DungeonSlot";
import {SourceIndexes, TargetIndexes} from "./SlotIndexes";

const DungeonContainer = styled.div`
    display: flex;
    flex-direction: row;
    margin-bottom: 20px;
`;

const DungeonCard = styled.img`
    width: 70px;
    height: 100px;
    margin-left: 10px;
`;


function Dungeon(props) {
    const { gameState } = useGameState()
    const cards = gameState.dungeonCards ?? gameState.dungeon.ranks.concat(gameState.dungeon.monsterPile)
    return (
        <DungeonContainer>
            {cards.map((card, index) => (
                <DungeonSlot
                    key={index}
                    cards={[card]}
                    index={TargetIndexes.DungeonIndex + index}
                    registerHovered={props.registerHovered}
                    registerDrop={props.registerDrop}
                />
            ))}
            <DungeonCard id={100} src={cardImages['Disease']} title={'Disease'} />
        </DungeonContainer>
    )
}

export default Dungeon;