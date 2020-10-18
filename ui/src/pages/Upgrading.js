import React, {useState} from "react";
import { useAuth } from "../context/auth";
import { Button, Options } from "../components/inputElements"
import {useGameState} from "../context/GameState";
import Dungeon from "../components/Dungeon";
import Village from "../components/Village";
import PlayerHand from "../components/PlayerHand"
import { DndProvider } from 'react-dnd'
import { HTML5Backend } from 'react-dnd-html5-backend'
import {removePurchased} from "../components/VillagePile";
import {UPGRADE_OFFSET} from "../components/UpgradeSlot";

function Upgrading(props) {
    const { gameState } = useGameState()
    const { authTokens } = useAuth()

    const  getUpgradedData = () => {
        const upgradedData = {}
        props.arrangement.forEach(column => {
            if (column[1] != null && column[1][1] != null) {
                upgradedData[column[0][0].name] = column[1][0].name
            }
        })
        return upgradedData;
    }

    const upgrade = () => {
        const upgradedData = getUpgradedData()
        props.gameSocket.send(JSON.stringify(
            {
                messageType: "Upgrade",
                data: {
                    gameId: gameState.gameId,
                    upgraded: upgradedData
                }
            }
        ))
    }

    const getUpgradedCard = (oldCard, newName) => {
        const oldName = oldCard.name
        let level = null
        let upgradedCard = null
        let purchasedCards = []
        const upgradedData = getUpgradedData()
        purchasedCards = Object.keys(upgradedData)
        const village = gameState.village
        village["heroes"].forEach((pile) => {
            if (pile.cards) {
                const cards = removePurchased(pile.cards, purchasedCards)
                cards.forEach(c => {
                    if (oldName === 'Militia' && c.name === newName) {
                        level = 0
                    }
                    if (level != null && c.level === level + 1) {
                        upgradedCard = c
                    } else if (c.name === oldName) {
                        level = c.level
                    }
                })
            }
            level = null
        })
        return upgradedCard
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
            if (Object.keys(getUpgradedData()).length === 0) {
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