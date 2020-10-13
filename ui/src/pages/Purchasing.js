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
import BuySlot from "../components/BuySlot";
import {SlotIndexes} from "../components/SlotIndexes"

function Purchasing(props) {
    const {gameState} = useGameState()
    const {authTokens} = useAuth()
    const [bought, setBought] = useState([])
    const [destroyed, setDestroyed] = useState([])
    const [cardsPurchased, setCardsPurchased] = useState([])
    const endTurn = () => {
        props.gameSocket.send(JSON.stringify(
            {
                messageType: "Purchase",
                data: {
                    gameId: gameState.gameId,
                    bought: bought.map(c => c.data.name),
                    destroyed: {},
                }
            }
        ))
    }

    const registerDrop = (source, target) => {
        if (target === SlotIndexes.BuyIndex) {
            let boughtCard = null
            let newPurchase = null
            const village = gameState.village
            const keys = ["heroes", "spells", "weapons", "items", "villagers"]
            keys.forEach(key => {
                village[key].forEach((pile, index) => {
                    if (pile.cards && pile.cards[0].name === source) {
                        boughtCard = {
                            index: SlotIndexes.BuyIndex + bought.length + 1,
                            data: {...pile.cards[0]}
                        }
                        newPurchase = {
                            key: key,
                            pile: index
                        }
                    }
                })
            })
            if (boughtCard) {
                const newBought = [...bought]
                newBought.push(boughtCard)
                setBought(newBought)
                const newCardsPurchased = [...cardsPurchased]
                newCardsPurchased.push(newPurchase)
                setCardsPurchased(newCardsPurchased)
            }
        } else if (target === null) {
            const cardIndex = source - SlotIndexes.BuyIndex
            if (cardIndex > 0 && cardIndex <= bought.length) {
                const newBought = [...bought]
                newBought.splice(cardIndex - 1, 1)
                setBought(newBought)
                const newCardsPurchased = [...cardsPurchased]
                newCardsPurchased.splice(cardIndex - 1, 1)
                setCardsPurchased(newCardsPurchased)
            }
        }
    }

    const renderChoices = () => {
        if (parseInt(authTokens.user.userId) === gameState.currentStage.data.currentPlayerId) {
            if (bought.length === props.attributes.buys) {
                return (
                    <Options key={5}>
                        <BuySlot cards={bought} registerHovered={props.registerHovered} registerDrop={registerDrop}
                                 index={SlotIndexes.BuyIndex}/>
                        <Button onClick={endTurn}>Done</Button>
                    </Options>
                )
            } else if (bought.length > props.attributes.buys) {
                return (
                    <div key={5}>
                        <div key={6} style={{fontSize: "x-large"}}>You do not have that many buys</div>
                        <Options key={7}>
                            <BuySlot key={8} cards={bought} registerHovered={props.registerHovered}
                                     registerDrop={registerDrop} index={SlotIndexes.BuyIndex} />
                        </Options>
                    </div>
                )
            } else {
                return (
                    <div key={5}>
                        <div key={6} style={{fontSize: "x-large"}}>You have more Buys available</div>
                        <Options key={7}>
                            <BuySlot key={8} cards={bought} registerHovered={props.registerHovered}
                                     registerDrop={registerDrop} index={SlotIndexes.BuyIndex} />
                            <Button key={9} onClick={endTurn}>Skip Buys</Button>
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
                <div key={1} style={disabledStyle}>
                    <Dungeon registerHovered={props.registerHovered} />
                </div>
                <Village key={2} registerHovered={props.registerHovered} registerDrop={props.registerDrop}
                         purchased={cardsPurchased} />
                <AttributeValues key={3} values={props.attributes} show={{
                    goldValue: "Gold",
                    buys: "Buys"
                }} />
                <PlayerHand key={4} registerHovered={props.registerHovered} registerDrop={registerDrop}
                            arrangement={props.arrangement} />
                {renderChoices()}
                {props.renderHovered()}
            </div>
        </DndProvider>
    )
}

export default Purchasing;