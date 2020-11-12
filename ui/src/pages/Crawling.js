import {useGameState} from "../context/GameState";
import React from "react";
import {Button, Options} from "../components/inputElements";
import {DndProvider} from "react-dnd";
import {HTML5Backend} from "react-dnd-html5-backend";
import battleImg from "../img/battle.png"
import Dungeon from "../components/Dungeon";
import Village from "../components/Village";
import PlayerHand from "../components/PlayerHand";
import AttributeValues from "../components/AttributeValues";
import {SourceIndexes, TargetIndexes} from "../components/SlotIndexes"
import {destroyForCards, getCardDestroysFromArrangement, serializeArrangement} from "../services/Arrangement";
import {CardTypes} from "../components/CardTypes";

function Crawling(props) {
    const {gameState, haveBanished, haveSentToBottom, remoteAttributes, sendMessage, renderHovered} = useGameState()
    const battle = () => {
        let finalArrangement = serializeArrangement(props.arrangement)
        const battling = props.arrangement[0].battling[0]
        sendMessage({
            messageType: "Battle",
            data: {
                monster: battling.data.sourceIndex - SourceIndexes.DungeonIndex,
                arrangement: finalArrangement,
            }
        })
    }

    const banish = () => {
        const destroyed = banishDestroy().data.name
        const dungeonNames = gameState.dungeonCards.map(c => c.data.name)
        sendMessage({
            messageType: "Banish",
            data: {
                dungeonOrder: dungeonNames,
                destroyed: destroyed
            }
        })
    }

    const doDestroy = () => {
        sendMessage({
            messageType: "Destroy",
            data: {
                cardNames: getCardDestroysFromArrangement(props.arrangement),
                borrowedDestroy: []
            }
        })
    }

    const sendToBottom = () => {
        const banished = gameState.dungeonCards[gameState.dungeon.ranks.length]
        sendMessage({
            messageType: "SendToBottom",
            data: {
                banished: banished.data.sourceIndex - SourceIndexes.DungeonIndex
            }
        })
    }

    const banishDestroy = () => {
        let result = null
        props.arrangement.forEach(slot => {
            let canBanish = false
            if (slot.cards[0].data.dungeonEffects) {
                slot.cards[0].data.dungeonEffects.forEach(e => {
                    if (e.effect === "Banish") {
                        canBanish = true
                    }
                })
            }
            if (canBanish) {
                if (slot.destroyed && slot.destroyed.length > 0) {
                    result = slot.destroyed[0]
                }
            }
        })
        return result
    }

    const renderChoices = () => {
        let options = []
        if (destroyForCards(props.arrangement)) {
            options.push((<Button key={100} onClick={doDestroy}>Destroy</Button>))
        }
        if (remoteAttributes['Banishes'] > 0 && haveBanished() && banishDestroy() != null) {
            options.push((<Button key={101} onClick={banish}>Banish</Button>))
        }
        if (remoteAttributes['SendToBottoms'] > 0 && haveSentToBottom()) {
            options.push((<Button key={102} onClick={sendToBottom}>Refill</Button>))
        }
        const battling = props.arrangement[0].battling[0]
        if (options.length === 0 && battling != null) {
            return (
                <Options key={5}>
                    <Button key={103} onClick={battle}>Battle</Button>
                </Options>
            )
        } else if (options.length === 0 && !haveBanished()) {
            return (
                <div key={6} style={{fontSize: "x-large"}}>Select a monster to battle</div>
            )
        } else if (remoteAttributes['Banishes'] > 0 && haveBanished())    {
            return (
                <div key={5}>
                    <div key={7} style={{fontSize: "x-large"}}>Destroy a card or reset dungeon</div>
                    <Options key={8}>
                        {options}
                    </Options>
                </div>
            )
        } else {
            return (<Options key={7}>
                {options}
            </Options>)
        }
    }

    const disabledStyle = {
        opacity: 0.4
    }

    return (
        <DndProvider backend={HTML5Backend}>
            <div>
                <Dungeon key={1}
                         extraSlot={{
                             image: battleImg,
                             title: "Battle",
                             altText: "Battle",
                             dropTypes: CardTypes.MONSTER,
                             cards: props.arrangement[0].battling ?? [],
                             index: TargetIndexes.BattleIndex,
                             singleDrop: true
                         }}
                />
                    />
                <div style={disabledStyle}>
                    <Village key={2} purchased={[]} />
                </div>
                <AttributeValues key={3} values={remoteAttributes} show={{
                    Light: "Light",
                    Attack: "Attack",
                    "Magic Attack": "Magic Attack"
                }} />
                <PlayerHand key={4} arrangement={props.arrangement} />
                {renderChoices()}
                {renderHovered()}
            </div>
        </DndProvider>
    )
}

export default Crawling;