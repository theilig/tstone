import {useGameState} from "../context/GameState";
import {useAuth} from "../context/auth";
import React, {useState} from "react";
import {Button, Options} from "../components/inputElements";
import {DndProvider} from "react-dnd";
import {HTML5Backend} from "react-dnd-html5-backend";
import Dungeon from "../components/Dungeon";
import Village from "../components/Village";
import PlayerHand from "../components/PlayerHand";
import AttributeValues from "../components/AttributeValues";
import {SourceIndexes, TargetIndexes} from "../components/SlotIndexes"
import BattleSlot from "../components/BatttleSlot";
import {getLowerMapFromArrangement} from "../services/Arrangement";

function Crawling(props) {
    const {gameState} = useGameState()
    const {authTokens} = useAuth()
    const [battling, setBattling] = useState(null)
    const [destroyed, setDestroyed] = useState({})
    const battle = () => {
        let finalArrangement = []
        props.arrangement.forEach(slot => {
            const equippedSlot = slot.cards
            const destroyedSlot = slot.destroy ?? []
            let mainCard = null
            let equippedCards = []
            let destroyedCards = []
            equippedSlot.forEach((card, index) => {
                if (index === 0) {
                    mainCard = card.data.name
                } else {
                    equippedCards.push(card.data.name)
                }
            })
            destroyedSlot.forEach(card => {
                destroyedCards.push(card.data.name)
            })
            finalArrangement.push({
                card: mainCard,
                equipped: equippedCards,
                destroyed: destroyedCards
            })
        })
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
        const banishList = getLowerMapFromArrangement(props.arrangement, "banish")
        const banished = Object.values(banishList).map(l => l[0])
        props.gameSocket.send(JSON.stringify(
            {
                messageType: "Banish",
                data: {
                    gameId: gameState.gameId,
                    banished: banished
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

    const renderChoices = () => {
        const banishList = getLowerMapFromArrangement(props.arrangement, "banish")
        if (Object.keys(banishList).length > 0) {
            return (<Options key={5}>
                <Button onClick={banish}>Banish</Button>
            </Options>)
        }
        if (battling != null) {
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
                <PlayerHand key={4} registerHovered={props.registerHovered} registerDrop={registerDrop}
                            registerDestroy={registerDestroy} arrangement={props.arrangement} />
                {renderChoices()}
                {props.renderHovered()}
            </div>
        </DndProvider>
    )
}

export default Crawling;