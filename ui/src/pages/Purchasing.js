import {useGameState} from "../context/GameState";
import {useAuth} from "../context/auth";
import React, {useState} from "react";
import {Button, Options} from "../components/inputElements";
import {DndProvider} from "react-dnd";
import {HTML5Backend} from "react-dnd-html5-backend";
import Dungeon from "../components/Dungeon";
import Village from "../components/Village";
import PlayerHand from "../components/PlayerHand";

function Purchasing(props) {
    const {gameState} = useGameState()
    const {authTokens} = useAuth()
    const [destroyed, setDestroyed] = useState(null)

    const endTurn = () => {
        props.gameSocket.send(JSON.stringify(
            {
                messageType: "Purchase",
                data: {
                    gameId: gameState.gameId,
                    cardName: destroyed
                }
            }
        ))
    }

    const registerDrop = (source, target) => {
        return true
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
                </div>
                <Village registerHovered={props.registerHovered}/>
                <PlayerHand registerHovered={props.registerHovered} registerDrop={registerDrop} mode={"Purchase"}/>
                {renderChoices()}
                {props.renderHovered()}
            </div>
        </DndProvider>
    )
}

export default Purchasing;