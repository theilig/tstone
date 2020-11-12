import React from "react";
import { Button, Options } from "../components/inputElements"
import {useGameState} from "../context/GameState";
import Dungeon from "../components/Dungeon";
import Village from "../components/Village";
import PlayerHand from "../components/PlayerHand"
import { DndProvider } from 'react-dnd'
import { HTML5Backend } from 'react-dnd-html5-backend'
import {getLowerMapFromArrangement} from "../services/Arrangement";

function Resting(props) {
    const {renderHovered, sendMessage } = useGameState()

    const endTurn = () => {
        const destroys = getLowerMapFromArrangement(props.arrangement, "destroyed")
        sendMessage({
            messageType: "Destroy",
            data: {
                // Resting throws the slot under the first card whatever it is, so we just pull
                // the data from the only key in the destroys object
                cardNames: {rest: destroys[Object.keys(destroys)[0]]},
                borrowedDestroy: []
            }
        })
    }

    const renderChoices = () => {
        const destroys = getLowerMapFromArrangement(props.arrangement, "destroyed")
        const keys = Object.keys(destroys)
        if (keys.length === 0 || destroys[keys[0]].length === 0) {
            return (
                <div>
                    <div style={{fontSize: "x-large"}}>You may destroy one card</div>
                    <Options>
                        <Button onClick={endTurn}>Skip Destroy</Button>
                    </Options>
                </div>
            )
        } else if (destroys[keys[0]].length > 1) {
            return (<div>
                <div style={{fontSize: "x-large"}}>You may only destroy one card</div>
            </div>)
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

export default Resting;