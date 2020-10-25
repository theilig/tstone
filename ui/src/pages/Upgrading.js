import React from "react";
import { useAuth } from "../context/auth";
import { Button, Options } from "../components/inputElements"
import {useGameState} from "../context/GameState";
import Dungeon from "../components/Dungeon";
import Village from "../components/Village";
import PlayerHand from "../components/PlayerHand"
import { DndProvider } from 'react-dnd'
import { HTML5Backend } from 'react-dnd-html5-backend'
import {removePurchased} from "../components/VillagePile";
import {getLowerMapFromArrangement} from "../services/Arrangement";
import AttributeValues from "../components/AttributeValues";

function Upgrading(props) {
    const { gameState } = useGameState()
    const { authTokens } = useAuth()

    const upgrade = () => {
        const upgradedData = getLowerMapFromArrangement(props.arrangement, "upgrade")
        // Normalize since we want to pass card => card and lower map is card => [card,...]
        Object.keys(upgradedData).forEach(key => {
            upgradedData[key] = upgradedData[key][0]
        })
        props.gameSocket.send(JSON.stringify(
            {
                messageType: "Upgrade",
                data: {
                    gameId: gameState.gameId,
                    upgrades: upgradedData
                }
            }
        ))
    }

    const getUpgradedCard = (oldCard, newName) => {
        const oldName = oldCard.data.name
        let level = null
        let upgradedCard = null
        let purchasedCards = []
        const upgradedData = getLowerMapFromArrangement(props.arrangement, "upgrade")
        purchasedCards = Object.keys(upgradedData)
        const village = gameState.village
        village["heroes"].forEach((pile) => {
            if (pile.cards) {
                const cards = removePurchased(pile.cards, purchasedCards)
                cards.forEach(c => {
                    if (oldName === 'Militia' && c.name === newName) {
                        level = 0
                    } else if (oldCard.data.heroType === c.heroType) {
                        level = oldCard.data.level
                    }
                    if (level != null && c.level === level + 1) {
                        upgradedCard = c
                    }
                })
            }
            level = null
        })
        // Village cards don't have a type when they come from the server, fixing that here
        // since we pulled straight from gameState
        return {cardType: 'HeroCard', data: upgradedCard}
    }

    const registerUpgrade = (oldCard, newName) => {
        let upgradedCard = null
        if (newName != null) {
            upgradedCard = getUpgradedCard(oldCard, newName)
        }
        return upgradedCard
    }


    const renderChoices = () => {
        if (parseInt(authTokens.user.userId) === gameState.currentStage.data.currentPlayerId) {
            if (Object.keys(getLowerMapFromArrangement(props.arrangement, "upgrade")).length === 0) {
                return (
                    <div>
                        <div style={{fontSize: "x-large"}}>You can upgrade</div>
                        <Options>
                            <Button onClick={upgrade}>End Turn</Button>
                        </Options>
                    </div>
                )
            } else {
                return (
                    <Options>
                        <Button onClick={upgrade}>Upgrade</Button>
                    </Options>
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
                <div style={disabledStyle}>
                    <Dungeon registerHovered={props.registerHovered} />
                </div>
                <Village registerHovered={props.registerHovered} upgrading={props.upgrading}/>
                <AttributeValues key={3} values={props.attributes} show={{
                    experience: "Experience"
                }} />
                <PlayerHand arrangement={props.arrangement}
                            registerHovered={props.registerHovered}
                            registerDrop={props.registerDrop}
                            registerUpgrade={registerUpgrade}
                            upgrading={props.upgrading}
                />
                {renderChoices()}
                {props.renderHovered()}
            </div>
        </DndProvider>
    )
}

export default Upgrading;