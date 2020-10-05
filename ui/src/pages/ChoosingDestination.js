import React from "react";
import { useAuth } from "../context/auth";
import { Button, Options } from "../components/inputElements"
import {useGameState} from "../context/GameState";
import Dungeon from "../components/Dungeon";
import Village from "../components/Village";
import PlayerHand from "../components/PlayerHand"
import { DndProvider } from 'react-dnd'
import { HTML5Backend } from 'react-dnd-html5-backend'

function ChoosingDestination(props) {
    const { gameState } = useGameState()
    const { authTokens } = useAuth()
    const renderChoices = () => {
        const stage = gameState.currentStage
        if (parseInt(authTokens.user.userId) === stage.data.currentPlayerId) {
            return (
                <Options>
                    <Button>Go To Village</Button>
                    <Button>Go To Dungeon</Button>
                    <Button onClick={chooseRest}>Rest</Button>
                </Options>
            )
        }
    }

    const chooseRest = () => {
        props.gameSocket.send(JSON.stringify(
            {
                messageType: "ChooseRest",
                data: {
                    gameId: gameState.gameId
                }
            }
        ))
    }

    return (
        <DndProvider backend={HTML5Backend}>
            <div>
                <Dungeon registerHovered={props.registerHovered} />
                <Village registerHovered={props.registerHovered} />
                <PlayerHand registerHovered={props.registerHovered} show={{
                    gold: "goldValue"
                }} />
                {renderChoices()}
                {props.renderHovered()}
            </div>
        </DndProvider>
    )
}

export default ChoosingDestination;