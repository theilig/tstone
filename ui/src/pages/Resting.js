import React, {useState} from "react";
import { useAuth } from "../context/auth";
import { Button, Options } from "../components/inputElements"
import {useGameState} from "../context/GameState";
import Dungeon from "../components/Dungeon";
import Village from "../components/Village";
import PlayerHand from "../components/PlayerHand"
import { DndProvider } from 'react-dnd'
import { HTML5Backend } from 'react-dnd-html5-backend'
import {getLowerMapFromArrangement} from "../services/Arrangement";

function Resting(props) {
    const { gameState } = useGameState()

    const endTurn = () => {
        const destroys = getLowerMapFromArrangement(props.arrangement, "destroy")
        props.gameSocket.send(JSON.stringify(
            {
                messageType: "Destroy",
                data: {
                    gameId: gameState.gameId,
                    // Resting throws the slot under the first card whatever it is, so we just pull
                    // the data from the only key in the destroys object
                    cardNames: {rest: destroys[Object.keys(destroys)[0]]}
                }
            }
        ))
    }

    const renderChoices = () => {
        const destroys = getLowerMapFromArrangement(props.arrangement, "destroy")
        if (Object.keys(destroys).length === 0) {
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

export default Resting;