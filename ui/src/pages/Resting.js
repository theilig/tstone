import React, {useState} from "react";
import { useAuth } from "../context/auth";
import { Button, Options } from "../components/inputElements"
import {useGameState} from "../context/GameState";
import Dungeon from "../components/Dungeon";
import Village from "../components/Village";
import PlayerHand from "../components/PlayerHand"
import { DndProvider } from 'react-dnd'
import { HTML5Backend } from 'react-dnd-html5-backend'

function Resting(props) {
    const { gameState } = useGameState()
    const { authTokens } = useAuth()
    const { destroyed, setDestroyed } = useState(null)

    const endTurn = () => {
        props.gameSocket.send(JSON.stringify(
            {
                messageType: "Destroy",
                data: {
                    gameId: gameState.gameId,
                    cardName: destroyed.data.cardName
                }
            }
        ))
    }

    const registerDestroy = (name) => {
        setDestroyed(name)
    }

    const renderChoices = () => {
        if (parseInt(authTokens.user.userId) === gameState.currentStage.data.currentPlayerId) {
            if (destroyed == null) {
                return (
                    <div>
                        <div style={{fontSize: "x-large"}}>You may destroy one card</div>
                        <Options>
                            <Button onClick={endTurn}>Skip Destroy</Button>
                        </Options>
                    </div>
                )
            } else {
                return (
                    <Options>
                        <Button onClick={endTurn}>Done</Button>
                        <Button>Undo Destroy</Button>
                    </Options>
                )
            }
        }
    }

    const disabledStyle = {
        opacity: 0.4
    }

    return (
        <DndProvider backend={HTML5Backend}>
            <div>
                <div style={disabledStyle}>
                    <Dungeon registerHovered={props.registerHovered} />
                    <Village registerHovered={props.registerHovered}/>
                </div>
                <PlayerHand registerHovered={props.registerHovered} registerDestroy={registerDestroy} show={{}}/>
                {renderChoices()}
                {props.renderHovered()}
            </div>
        </DndProvider>
    )
}

export default Resting;