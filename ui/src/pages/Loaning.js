import {useGameState} from "../context/GameState";
import React, {useState} from "react";
import {Button, Options} from "../components/inputElements";
import {DndProvider} from "react-dnd";
import {HTML5Backend} from "react-dnd-html5-backend";
import Dungeon from "../components/Dungeon";
import Village from "../components/Village";
import PlayerHand from "../components/PlayerHand";
import AttributeValues from "../components/AttributeValues";
import {TargetIndexes} from "../components/SlotIndexes"
import LoanSlot from "../components/LoanSlot";
import {useAuth} from "../context/auth";

function Loaning(props) {
    const {gameState} = useGameState()
    const [loaned, setLoaned] = useState(null)
    const endTurn = () => {
        props.gameSocket.send(JSON.stringify(
            {
                messageType: "Loan",
                data: {
                    gameId: gameState.gameId,
                    hero: loaned.data.name,
                }
            }
        ))
    }

    const registerDrop = (source, target) => {
        if (target === TargetIndexes.LoanIndex) {
            if (loaned) {
               props.registerDrop(loaned, null)
            }
            setLoaned(source)
        }
        props.registerDrop(source, target)
    }

    const renderChoices = () => {
        if (loaned) {
            return (
                <Options key={5}>
                    <LoanSlot cards={[loaned]} registerHovered={props.registerHovered} registerDrop={registerDrop}
                             index={TargetIndexes.LoanIndex}/>
                    <Button onClick={endTurn}>Done</Button>
                </Options>
            )
        } else {
            return (
                <div key={5}>
                    <div key={6} style={{fontSize: "x-large"}}>You must loan a Hero</div>
                    <Options key={7}>
                        <LoanSlot key={8} cards={[]} registerHovered={props.registerHovered}
                                 registerDrop={registerDrop} index={TargetIndexes.LoanIndex} />
                    </Options>
                </div>
            )
        }
    }

    const disabledStyle = {
        opacity: 0.4
    }

    return (
        <DndProvider backend={HTML5Backend}>
            <div>
                <div key={1} style={disabledStyle}>
                    <Dungeon registerHovered={props.registerHovered} />
                </div>
                <Village key={2} registerHovered={props.registerHovered} registerDrop={props.registerDrop}
                         purchased={[]} />
                <AttributeValues key={3} values={props.attributes} show={{
                    goldValue: "Gold",
                    buys: "Buys",
                    experience: "Experience"
                }} />
                <PlayerHand key={4} registerHovered={props.registerHovered} registerDrop={registerDrop}
                            arrangement={props.arrangement} />
                {renderChoices()}
                {props.renderHovered()}
            </div>
        </DndProvider>
    )
}

export default Loaning;