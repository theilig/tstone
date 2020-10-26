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
import {SourceIndexes, TargetIndexes} from "../components/SlotIndexes"

function TakingSpoils(props) {
    const {gameState, haveSentToBottom} = useGameState()
    const [bought, setBought] = useState([])
    const endTurn = () => {
        let banished = null
        let banishedCard = null
        if (gameState.dungeonCards) {
            banishedCard = gameState.dungeonCards[gameState.dungeon.ranks.length]
            banished = banishedCard.data.sourceIndex - SourceIndexes.DungeonIndex
        }
        let data = {
            gameId: gameState.gameId,
            bought: bought.map(c => c.data.name),
        }
        if (banished != null && banishedCard && banishedCard.data.name !== 'CardBack') {
            data.sentToBottom = banished
        }
        props.gameSocket.send(JSON.stringify(
            {
                messageType: "TakeSpoils",
                data: data
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
        let needBuySlot = false
        props.spoils.forEach(s => {
            if (s !== "SendToBottom") {
                needBuySlot = true
            }
        })
        if (bought.length > 0 || haveSentToBottom()) {
            return (
                <Options key={5}>
                    <div key={6} style={{fontSize: "x-large"}}>You can take {props.spoils.join(',')}</div>
                    {needBuySlot && (<BuySlot cards={bought} registerHovered={props.registerHovered} registerDrop={registerDrop}
                             index={TargetIndexes.BuyIndex}/>)}
                    <Button onClick={endTurn}>Done</Button>
                </Options>
            )
        } else {
            return (
                <div key={5}>
                    <div key={6} style={{fontSize: "x-large"}}>You can take {props.spoils.join(',')}</div>
                    <Options key={7}>
                        {needBuySlot && (<BuySlot key={8} cards={bought} registerHovered={props.registerHovered}
                                 registerDrop={registerDrop} index={TargetIndexes.BuyIndex} />)}
                        <Button key={9} onClick={endTurn}>Skip Spoils</Button>
                    </Options>
                </div>
            )
        }
    }

    const disabledStyle = {
        opacity: 0.4
    }

    const regularStyle = {

    }

    let dungeonStyle = disabledStyle
    if (props.spoils.includes("SendToBottom")) {
        dungeonStyle = regularStyle
    }


    return (
        <DndProvider backend={HTML5Backend}>
            <div>
                <div key={1} style={dungeonStyle}>
                    <Dungeon registerHovered={props.registerHovered} registerDrop={props.registerDrop}/>
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