import React from "react";
import {useGameState} from "../context/GameState";
import cardImages from "../img/cards/cards"
import styled from "styled-components";
import {HandCard} from "./HandCard";

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

    return (
        <DungeonContainer>
            {gameState.dungeon.monsterPile.map((card, index) => (
                 <HandCard
                     key={card.data.sourceIndex}
                     small={true}
                     card={card}
                     position={0}
                     registerHovered={props.registerHovered}
                     registerDrop={props.registerDrop}
                     style={{
                         zIndex: index
                    }}
                 />
            ))}
            <DungeonCard id={100} src={cardImages['Disease']} title={'Disease'} />
        </DungeonContainer>
    )
}

export default Dungeon;