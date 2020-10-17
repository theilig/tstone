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
    const [ upgraded, setUpgraded ] = useState({})

    const upgrade = () => {
        const upgradedData = {}
        Object.keys(upgraded).forEach(key => {
            const card = upgraded[key]
            upgradedData[key] = card.name
        })
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

/*    const registerDrop = (source, target) => {
    }
        if (target - target % 100 === UPGRADE_OFFSET) {
            let upgradedCard = null
            const village = gameState.village
            village["heroes"].forEach((pile) => {
                if (pile.cards) {
                    Object.keys(upgraded).forEach(key => {
                        purchasedCards.push(upgrade[key].name)
                    })
                    const cards = removePurchased(pile.cards, purchasedCards)
                    cards.forEach(c => {
                        if (c.level === props.upgradee.data.level + 1) {
                            upgradedCard = c
                        }
                    })
                    if (upgradedCard) {
                        let newData = {...upgradedCard.data}
                        newData.index = target + 50
                        props.registerDrop(newData, target)
                        let newUpgraded = [...upgraded]
                        newUpgraded[newData.index] = newData
                        setUpgraded(newUpgraded)
                    }
                }
            })
        } else if (target === null) {
            let newUpgraded = [...upgraded]
            newUpgraded[target + 50] = null
            setUpgraded(newUpgraded)
        }
        props.registerDrop(source, target)
     }

 */

    const getUpgradedName = (oldName, newName) => {
        let level = null
        let upgradedName = null
        let purchasedCards = []
        Object.keys(upgraded).forEach(key => {
            purchasedCards.push(upgraded[key])
        })
        const village = gameState.village
        village["heroes"].forEach((pile) => {
            if (pile.cards) {
                const cards = removePurchased(pile.cards, purchasedCards)
                cards.forEach(c => {
                    if (oldName === 'Militia' && c.name === newName) {
                        level = 0
                    }
                    if (level != null && c.level === level + 1) {
                        upgradedName = c.name
                    } else if (c.name === oldName) {
                        level = c.level
                    }
                })
            }
            level = null
        })
        return upgradedName
    }

    const registerUpgrade = (oldName, newName) => {
        let newUpgraded = {...upgraded}
        let upgradedName = null
        if (newName != null) {
            upgradedName = getUpgradedName(oldName, newName)
            newUpgraded[oldName] = upgradedName
        } else {
            delete newUpgraded[oldName]
        }
        setUpgraded(newUpgraded)
        return upgradedName
    }


    const renderChoices = () => {
        if (parseInt(authTokens.user.userId) === gameState.currentStage.data.currentPlayerId) {
            if (Object.keys(upgraded).length === 0) {
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