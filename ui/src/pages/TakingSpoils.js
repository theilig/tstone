import {useGameState} from "../context/GameState";
import React, {useState} from "react";
import {Button, Options} from "../components/inputElements";
import {DndProvider} from "react-dnd";
import {HTML5Backend} from "react-dnd-html5-backend";
import Dungeon from "../components/Dungeon";
import Village from "../components/Village";
import PlayerHand from "../components/PlayerHand";
import AttributeValues from "../components/AttributeValues";
import BuySlot from "../components/BuySlot";
import {TargetIndexes} from "../components/SlotIndexes"
import {destroyForCards, getCardDestroysFromArrangement, getLowerMapFromArrangement} from "../services/Arrangement";

function TakingSpoils(props) {
    const {gameState} = useGameState()
    const [bought, setBought] = useState([])
    const endTurn = () => {
        props.gameSocket.send(JSON.stringify(
            {
                messageType: "Purchase",
                data: {
                    gameId: gameState.gameId,
                    bought: bought.map(c => c.data.name),
                    destroyed: getLowerMapFromArrangement(props.arrangement, "destroy"),
                }
            }
        ))
    }

    const registerDrop = (source, target) => {
        if (target === TargetIndexes.BuyIndex) {
            const newBought = [...bought]
            newBought.push(source)
            setBought(newBought)
        } else if (target === null) {
            const boughtIndexes = bought.map(c => c.sourceIndex)
            const removedIndex = boughtIndexes.indexOf(source.sourceIndex)
            if (removedIndex >= 0) {
                const newBought = [...bought]
                newBought.splice(removedIndex, 1)
                setBought(newBought)
            }
        }
        props.registerDrop(source, target)
    }

    const renderChoices = () => {
        if (bought.length > 0) {
            return (
                <Options key={5}>
                    <div key={6} style={{fontSize: "x-large"}}>You can take {props.spoils.join(',')}</div>
                    <BuySlot cards={bought} registerHovered={props.registerHovered} registerDrop={registerDrop}
                             index={TargetIndexes.BuyIndex}/>
                    <Button onClick={endTurn}>Done</Button>
                </Options>
            )
        } else {
            return (
                <div key={5}>
                    <div key={6} style={{fontSize: "x-large"}}>You can take {props.spoils.join(',')}</div>
                    <Options key={7}>
                        <BuySlot key={8} cards={bought} registerHovered={props.registerHovered}
                                 registerDrop={registerDrop} index={TargetIndexes.BuyIndex} />
                        <Button key={9} onClick={endTurn}>Skip Spoils</Button>
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
                         purchased={bought.map(c => c.data.name)} />
                <AttributeValues key={3} values={props.attributes} show={{
                    goldValue: "Gold",
                    buys: "Buys",
                }} />
                <PlayerHand key={4} registerHovered={props.registerHovered} registerDrop={registerDrop}
                            arrangement={props.arrangement} />
                {renderChoices()}
                {props.renderHovered()}
            </div>
        </DndProvider>
    )
}

export default TakingSpoils;