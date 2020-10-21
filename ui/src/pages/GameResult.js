import {useGameState} from "../context/GameState";
import React, {useState} from "react";
import {Button, Options} from "../components/inputElements";
import PlayerResult from "../components/PlayerResult";
import styled from "styled-components";

const ResultContainer = styled.div`
    display: flex;
    flex-direction: row;
    margin-bottom: 20px;
`;


function GameResult(props) {
    const {gameState} = useGameState()
    return (
        <div>
            <ResultContainer>
                {gameState.players.map(p => {
                    const cards = p.hand.concat(p.discard, p.deck)
                    return (<PlayerResult registerHovered={props.registerHovered}
                                          cards={cards}
                                          name={p.name}
                    />)
                })}
                {props.renderHovered()}
            </ResultContainer>
            <Button onClick={props.leaveGame}>Leave Game</Button>
        </div>
    )
}

export default GameResult;