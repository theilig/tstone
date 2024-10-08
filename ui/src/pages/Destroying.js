import React from "react";
import { Button, Options } from "../components/inputElements"
import { useGameState } from "../context/GameState";
import Dungeon from "../components/Dungeon";
import Village from "../components/Village";
import PlayerHand from "../components/PlayerHand"
import { DndProvider } from 'react-dnd'
import { HTML5Backend } from 'react-dnd-html5-backend'
import {getLowerMapFromArrangement} from "../services/Arrangement";

function Destroying(props) {
    const { gameState, sendMessage, renderHovered } = useGameState()

    const endTurn = () => {
        sendMessage({
            messageType: "Destroy",
            data: {
                cardNames: getLowerMapFromArrangement(props.arrangement, "destroyed"),
                borrowedDestroy: []
            }
        })
    }

    const renderChoices = () => {
        const required = gameState.currentStage.stage === "DiscardOrDestroy" ? 0 : 1
        if (required > 0 &&
            Object.keys(getLowerMapFromArrangement(props.arrangement, "destroyed")).length !== required) {
            return (
                <div>
                    <div style={{fontSize: "x-large"}}>You must destroy {required} card(s)</div>
                </div>
            )
        } else {
            return (
                <Options>
                    {required === 0 && (<div style={{fontSize: "x-large"}}>You may destroy card(s)</div>)}
                    <Button onClick={endTurn}>Done</Button>
                </Options>
            )
        }
    }

    const disabledStyle = {
        opacity: 0.4
    }

    return (
        <DndProvider backend={HTML5Backend}>
            <div>
                <div style={disabledStyle}>
                    <Dungeon />
                    <Village />
                </div>
                <PlayerHand arrangement={props.arrangement} />
                {renderChoices()}
                {renderHovered()}
            </div>
        </DndProvider>
    )
}

export default Destroying;