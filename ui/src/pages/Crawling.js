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
import {SlotIndexes} from "../components/SlotIndexes"
import BattleSlot from "../components/BatttleSlot";

function Crawling(props) {
    const {gameState} = useGameState()
    const {authTokens} = useAuth()
    const [battling, setBattling] = useState(null)
    const [destroyed, setDestroyed] = useState([])
    const [cardsPurchased, setCardsPurchased] = useState([])
    const battle = () => {
        let finalArrangement = []
        props.arrangement.forEach(slot => {
            const equippedSlot = slot[0]
            const destroyedSlot = slot[1] ?? []
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
                    monster: battling,
                    arrangement: finalArrangement,
                }
            }
        ))
    }

    const registerDrop = (source, target) => {
        if (target === SlotIndexes.BattleIndex) {
            setBattling(source)
        } else if (target === null && source === battling) {
            setBattling(null)
        } else {
            props.registerDrop(source, target)
        }
    }

    const renderChoices = () => {
        if (parseInt(authTokens.user.userId) === gameState.currentStage.data.currentPlayerId) {
            if (battling != null) {
                const monsterCard = gameState.dungeon.monsterPile[battling]
                return (
                    <Options key={5}>
                        <BattleSlot card={monsterCard} registerHovered={props.registerHovered} registerDrop={registerDrop}
                                 index={SlotIndexes.BattleIndex} />
                        <Button onClick={battle}>Battle</Button>
                    </Options>
                )
            } else {
                return (
                    <div key={5}>
                        <div key={6} style={{fontSize: "x-large"}}>Select a monster to battle</div>
                        <Options key={7}>
                            <BattleSlot key={8} card={null} registerHovered={props.registerHovered}
                                     registerDrop={registerDrop} index={SlotIndexes.BattleIndex} />
                        </Options>
                    </div>
                )
            }
        }
    }

    const disabledStyle = {
        opacity: 0.4
    }

    return (
        <DndProvider backend={HTML5Backend}>
            <div>
                <Dungeon key={1} registerHovered={props.registerHovered} />
                <div style={disabledStyle}>
                    <Village key={2} registerHovered={props.registerHovered} registerDrop={props.registerDrop}
                             purchased={cardsPurchased} />
                </div>
                <AttributeValues key={3} values={props.attributes} show={{
                    light: "Light",
                    attack: "Attack",
                    magicAttack: "Magic Attack"
                }} />
                <PlayerHand key={4} registerHovered={props.registerHovered} registerDrop={registerDrop}
                            arrangement={props.arrangement} />
                {renderChoices()}
                {props.renderHovered()}
            </div>
        </DndProvider>
    )
}

export default Crawling;