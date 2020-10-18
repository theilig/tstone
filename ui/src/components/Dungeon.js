import React, {useEffect, createRef, useState} from "react";
import {useGameState} from "../context/GameState";
import cardImages from "../img/cards/cards"
import styled from "styled-components";
import {HandCard} from "./HandCard";
import {getDragType} from "./CardTypes";

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
    const [monsterRefs, setMonsterRefs] = useState([]);
    const handleHovered = (index) => {
        if (monsterRefs[index] && monsterRefs[index].current) {
            props.registerHovered(gameState.dungeon.monsterPile[index].data.name,
                monsterRefs[index].current.getBoundingClientRect())
        }
    }

    useEffect(() => {
        if (gameState.dungeon.monsterPile) {
            let pile = gameState.dungeon.monsterPile
            setMonsterRefs(monsterRefs => (
                Array(pile.length).fill(null).map((_, i) => monsterRefs[i] || createRef())
            ))
        }
    }, [gameState.dungeon.monsterPile]);

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