import {useGameState} from "../context/GameState";
import React, {useState} from "react";
import {Button, Options} from "../components/inputElements";
import {DndProvider} from "react-dnd";
import {HTML5Backend} from "react-dnd-html5-backend";
import Dungeon from "../components/Dungeon";
import Village from "../components/Village";
import PlayerHand from "../components/PlayerHand";
import AttributeValues from "../components/AttributeValues";
import {SourceIndexes, TargetIndexes} from "../components/SlotIndexes"
import BattleSlot from "../components/BattleSlot";
import {destroyForCards, getCardDestroysFromArrangement, serializeArrangement} from "../services/Arrangement";

function Crawling(props) {
    const {gameState, haveBanished, haveSentToBottom, remoteAttributes} = useGameState()
    const [battling, setBattling] = useState(null)
    const [destroyed, setDestroyed] = useState({})
    const battle = () => {
        let finalArrangement = serializeArrangement(props.arrangement)
        props.gameSocket.send(JSON.stringify(
            {
                messageType: "Battle",
                data: {
                    gameId: gameState.gameId,
                    monster: battling.data.sourceIndex - SourceIndexes.DungeonIndex,
                    arrangement: finalArrangement,
                }
            }
        ))
    }

    const banish = () => {
        const destroyed = banishDestroy().data.name
        const dungeonNames = gameState.dungeonCards.map(c => c.data.name)
        props.gameSocket.send(JSON.stringify(
            {
                messageType: "Banish",
                data: {
                    gameId: gameState.gameId,
                    dungeonOrder: dungeonNames,
                    destroyed: destroyed
                }
            }
        ))
    }

    const doDestroy = () => {
        props.gameSocket.send(JSON.stringify(
            {
                messageType: "Destroy",
                data: {
                    gameId: gameState.gameId,
                    cardNames: getCardDestroysFromArrangement(props.arrangement)
                }
            }
        ))
    }

    const sendToBottom = () => {
        const banished = gameState.dungeonCards[gameState.dungeon.ranks.length]
        props.gameSocket.send(JSON.stringify(
            {
                messageType: "SendToBottom",
                data: {
                    gameId: gameState.gameId,
                    banished: banished.data.name
                }
            }
        ))
    }

    const registerDestroy = (name, destroyerName) => {
        let newDestroyed = {...destroyed}
        newDestroyed[destroyerName] = destroyed[destroyerName] ?? []
        newDestroyed[destroyerName].push(name)
        setDestroyed(newDestroyed)
    }

    const registerDrop = (source, targetIndex) => {
        if (targetIndex === TargetIndexes.BattleIndex) {
            setBattling(source)
        } else if (battling && targetIndex === null && source.data.sourceIndex === battling.data.sourceIndex) {
            setBattling(null)
        } else {
            props.registerDrop(source, targetIndex)
        }
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
            options.push((<Button onClick={doDestroy}>Destroy</Button>))
        }
        if (props.attributes.banishes > 0 && haveBanished() && banishDestroy() != null) {
            options.push((<Button onClick={banish}>Banish</Button>))
        }
        if (props.attributes.sendToBottoms > 0 && haveSentToBottom()) {
            options.push((<Button onclick={sendToBottom}>Refill</Button>))
        }
        if (options.length === 0 && battling != null) {
            return (
                <Options key={5}>
                    <BattleSlot card={battling} registerHovered={props.registerHovered} registerDrop={registerDrop}
                             index={TargetIndexes.BattleIndex} />
                    <Button onClick={battle}>Battle</Button>
                </Options>
            )
        } else {
            return (
                <div key={5}>
                    <div key={6} style={{fontSize: "x-large"}}>Select a monster to battle</div>
                    <Options key={7}>
                        {options}
                        <BattleSlot key={8} card={null} registerHovered={props.registerHovered}
                                 registerDrop={registerDrop} index={TargetIndexes.BattleIndex} />
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
                <Dungeon key={1} registerHovered={props.registerHovered} registerDrop={props.registerDrop} />
                <div style={disabledStyle}>
                    <Village key={2} registerHovered={props.registerHovered} registerDrop={props.registerDrop}
                             purchased={[]} />
                </div>
                <AttributeValues key={3} values={props.attributes} show={{
                    light: "Light",
                    attack: "Attack",
                    magicAttack: "Magic Attack"
                }} />
                <AttributeValues key={6} values={remoteAttributes} show={{
                    Light: "Light",
                    Attack: "Attack",
                    "Magic Attack": "Magic Attack"
                }} />
                <PlayerHand key={4} registerHovered={props.registerHovered} registerDrop={registerDrop}
                            registerDestroy={registerDestroy} arrangement={props.arrangement} />
                {renderChoices()}
                {props.renderHovered()}
            </div>
        </DndProvider>
    )
}

export default Crawling;