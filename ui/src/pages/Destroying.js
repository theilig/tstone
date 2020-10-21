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
    const { gameState } = useGameState()

    const endTurn = () => {
        props.gameSocket.send(JSON.stringify(
            {
                messageType: "Destroy",
                data: {
                    gameId: gameState.gameId,
                    cardNames: getLowerMapFromArrangement(props.arrangement, "destroy")
                }
            }
        ))
    }

    const renderChoices = () => {
        const required = gameState.currentStage.data.minRequired ?? 0
        if (Object.keys(getLowerMapFromArrangement(props.arrangement, "destroy")).length !== required) {
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
                    <Dungeon registerHovered={props.registerHovered} />
                    <Village registerHovered={props.registerHovered}/>
                </div>
                <PlayerHand arrangement={props.arrangement}
                            registerHovered={props.registerHovered}
                            registerDrop={props.registerDrop}
                />
                {renderChoices()}
                {props.renderHovered()}
            </div>
        </DndProvider>
    )
}

export default Destroying;