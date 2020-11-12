import React from "react";
import {useGameState} from "../context/GameState";
import {Button, Options} from "../components/inputElements";
import {DndProvider} from "react-dnd";
import {HTML5Backend} from "react-dnd-html5-backend";
import Dungeon from "../components/Dungeon";
import Village from "../components/Village";
import PlayerHand from "../components/PlayerHand";
import AttributeValues from "../components/AttributeValues";
import {SourceIndexes, TargetIndexes} from "../components/SlotIndexes"
import buyImg from "../img/buy.png";
import {CardTypes} from "../components/CardTypes";

function TakingSpoils(props) {
    const {gameState, haveSentToBottom, remoteAttributes, sendMessage, renderHovered} = useGameState()
    const endTurn = () => {
        let banished = null
        let banishedCard = null
        if (gameState.dungeonCards) {
            banishedCard = gameState.dungeonCards[gameState.dungeon.ranks.length]
            banished = banishedCard.data.sourceIndex - SourceIndexes.DungeonIndex
        }
        const bought = props.arrangement[0].buying
        let data = {
            gameId: gameState.gameId,
            bought: bought.map(c => c.data.name),
        }
        if (banished != null && banishedCard && banishedCard.data.name !== 'CardBack') {
            data.sentToBottom = banished
        }
        sendMessage({
            messageType: "TakeSpoils",
            data: data
        })
    }

    const renderChoices = () => {
        const spoilsChoices = props.spoils.filter(s => {
            return s !== "SendToBottom"
        })

        const sendToBottoms = props.spoils.filter(s => {
            return s === "SendToBottom"
        })


        let spoilsList = ""

        if (spoilsChoices.length > 0) {
            spoilsList = spoilsChoices.join(',')
        }

        let sendToBottomsList = ''

        if (sendToBottoms.length === 1) {
            sendToBottomsList = "send one monster to bottom"
        } else if (sendToBottoms.length > 1) {
            sendToBottomsList = "send " + sendToBottoms.length + " monsters to bottom"
        }

        let spoilsString = ''

        if (spoilsList !== '') {
            spoilsString = 'You can take ' + spoilsList
        }
        if (sendToBottomsList !== '') {
            if (spoilsList !== '') {
                spoilsString = spoilsString + ' and ' + sendToBottomsList
            } else {
                spoilsString = 'You can ' + sendToBottomsList
            }
        }
        const bought = props.arrangement[0].buying

        if (bought.length > 0 || haveSentToBottom()) {
            return (
                <div>
                    <div key={6} style={{fontSize: "x-large"}}>{spoilsString}</div>
                    <Options key={5}>
                        <Button onClick={endTurn}>Done</Button>
                    </Options>
                </div>
            )
        } else {
            return (
                <div key={5}>
                    <div key={6} style={{fontSize: "x-large"}}>{spoilsString}</div>
                    <Options key={7}>
                        <Button key={9} onClick={endTurn}>Skip Spoils</Button>
                    </Options>
                </div>
            )
        }
    }

    const bought = props.arrangement[0].buying
    let extraSlot = null
    props.spoils.forEach(s => {
        if (s !== "SendToBottom") {
            extraSlot = {
                image: buyImg,
                    title: "Buy",
                    altText: "Buy",
                    dropTypes: CardTypes.VILLAGE,
                    cards: bought,
                    index: TargetIndexes.BuyIndex,
                    singleDrop: false
            }
        }
    })

    return (
        <DndProvider backend={HTML5Backend}>
            <Dungeon
                key={1}
                extraSlot={extraSlot}
            />
            <Village key={2} purchased={bought.map(c => c.data.name)} />
            <AttributeValues key={3} values={remoteAttributes} show={{
                Gold: "Gold",
            }} />
            <PlayerHand key={4} arrangement={props.arrangement} />
            {renderChoices()}
            {renderHovered()}
        </DndProvider>
    )
}

export default TakingSpoils;