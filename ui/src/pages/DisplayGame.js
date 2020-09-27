import React, { useState } from "react";
import { useAuth } from "../context/auth";
import styled from "styled-components";
import {Button, Error} from "../components/AuthForm"
import {useGameState} from "../context/GameState";
import cardImages from "../img/cards/cards"
import Dungeon from "../components/Dungeon";
import Village from "../components/Village";
import PlayerHand from "../components/PlayerHand"
import { DndProvider } from 'react-dnd'
import { HTML5Backend } from 'react-dnd-html5-backend'

const Options = styled.div`
    display: flex;
    flex-direction: row;
    max-width:410px;
`;


function DisplayGame(props) {
    const { gameState } = useGameState()
    const { authTokens } = useAuth()
    const renderChoices = (stage) => {
        if (parseInt(authTokens.user.userId) === stage.data.currentPlayerId) {
            return (
                <Options>
                    <Button>Go To Village</Button>
                    <Button>Go To Dungeon</Button>
                    <Button>Rest</Button>
                </Options>
            )
        }
    }
    return (
        <DndProvider backend={HTML5Backend}>
            <div>
                <Dungeon />
                <Village />
                <PlayerHand />
                {renderChoices(gameState.currentStage)}
            </div>
        </DndProvider>
    )
}

export default DisplayGame;