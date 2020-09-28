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
import {HandCard} from "../components/HandCard";

const Options = styled.div`
    display: flex;
    flex-direction: row;
    max-width:410px;
`;


function DisplayGame(props) {
    const { gameState } = useGameState()
    const { authTokens } = useAuth()
    const [ hovered, setHovered] = useState(null)
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
    const registerHovered = (card, location) => {
        setHovered({card:card, location:location})
    }

    const renderHovered = () => {
        if (hovered) {
            const imgStyle = {
                width: '300px',
                height: '375px',
                position: 'absolute',
                top: hovered.location.top,
                left: hovered.location.left,
                'pointer-events' : 'none'
            };
            return (<img style={imgStyle} src={cardImages[hovered.card.name]} title={hovered.card.name} alt={hovered.card.name} />)
        }
    }

    return (
        <DndProvider backend={HTML5Backend}>
            <div>
                <Dungeon registerHovered={registerHovered} />
                <Village registerHovered={registerHovered} />
                <PlayerHand registerHovered={registerHovered} />
                {renderChoices(gameState.currentStage)}
                {renderHovered()}
            </div>
        </DndProvider>
    )
}

export default DisplayGame;