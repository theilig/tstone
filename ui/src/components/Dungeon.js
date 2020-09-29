import React, {useEffect, createRef, useState} from "react";
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
    width: 140px;
    height: 200px;
    margin-left: 10px;
`;


function Dungeon(props) {
    const { gameState } = useGameState()
    const [monsterRefs, setMonsterRefs] = useState([]);
    const handleHovered = (index) => {
        if (monsterRefs[index] && monsterRefs[index].current) {
            props.registerHovered(gameState.dungeon.monsterPile[index].data,
                monsterRefs[index].current.getBoundingClientRect())
        }
    }

    useEffect(() => {
        if (gameState.dungeon.monsterPile) {
            let pile = gameState.dungeon.monsterPile
            setMonsterRefs(monsterRefs => (
                Array(pile.length).fill().map((_, i) => monsterRefs[i] || createRef())
            ))
        }
    }, [gameState.dungeon.monsterPile]);

    return (
        <DungeonContainer>
            {gameState.dungeon.monsterPile.map((card, index) => (
                <DungeonCard key={index} id={(card.data.id ?? 0) + (index + 1) * 1000}
                             src={cardImages[card.data.name]} title={card.data.name}
                             onMouseOver={() => handleHovered(index)}
                             ref={monsterRefs[index]}
                             onMouseOut={() => props.registerHovered(null, null)}
                />
            ))}
            <DungeonCard id={100} src={cardImages['Disease']} title={'Disease'} />
        </DungeonContainer>
    )
}

export default Dungeon;