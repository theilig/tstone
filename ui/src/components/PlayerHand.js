import React, { useState, useEffect } from "react";
import {useGameState} from "../context/GameState";
import { useAuth } from "../context/auth";
import styled from "styled-components";

import {isGeneralEffect, executeEffect, isLateEffect} from "../services/effects"
import HandSlot from "./HandSlot";
import {getAttributes} from "./HandCard";

const StatusContainer = styled.div`
    display: flex;
    flex-direction: column;
`;

const HandContainer = styled.div`
    display: flex;
    flex-direction: row;
    margin-bottom: 20px;
`;


function PlayerHand(props) {
    const { gameState } = useGameState()
    const { authTokens } = useAuth()
    const [ player, setPlayer ] = useState(null)
    const [ isAttached, setIsAttached ] = useState([])
    const [ using, setUsing ] = useState([])
    const [ cardSlots, setCardSlots ] = useState([])
    const [ attributes, setAttributes ] = useState({})

    const registerDrop = (source, target) => {
        let newAttached = [...isAttached]
        if (newAttached[source]) {
            newAttached[source] = null
        }
        if (target) {
            let newUsing = [...using]
            newAttached[source] = target
            let usingList = newUsing[target] ?? []
            usingList.push(player.hand[source])
            newUsing[target] = usingList
            setUsing(newUsing)
        }
        setIsAttached(newAttached)
    }
    const addGeneralEffects = (card) => {
        let generalEffects = []
        if (card.data.dungeonEffects) {
            card.data.dungeonEffects.forEach((effect) => {
                if (isGeneralEffect(effect)) {
                    generalEffects.push(effect)
                }
            })
        }
        if (card.data.villageEffects) {
            card.data.villageEffects.forEach((effect) => {
                if (isGeneralEffect(effect)) {
                    generalEffects.push(effect)
                }
            })
        }
        return generalEffects
    }

    const addLateEffects = (card, index, currentAttributes) => {
        let newAttributes = {...currentAttributes}
        if (card.data.dungeonEffects) {
            card.data.dungeonEffects.forEach((effect) => {
                if (isLateEffect(effect)) {
                    newAttributes = executeEffect(effect, newAttributes)
                }
            })
        }
        if (card.data.villageEffects) {
            card.data.villageEffects.forEach((effect) => {
                if (isLateEffect(effect)) {
                    newAttributes = executeEffect(effect, newAttributes)
                }
            })
        }
        return newAttributes
    }
    const mergeObjects = (dest, source) => {
        let newDest = {...dest}
        Object.keys(source).forEach((key) => {
            let value = source[key]
            if (key in newDest) {
                newDest[key] += value;
            }
        })
        return newDest
    }

    const sumAttributes = () => {
        let starting = {
            goldValue: 0,
            light: 0,
            attack: 0,
            magicAttack: 0,
            strength: 0,
            weight: 0,
        }
        Object.keys(attributes).forEach((index) => {
            let cardAttributes = attributes[index]
            Object.keys(cardAttributes).forEach((key) => {
                let value = cardAttributes[key]
                if (key in starting) {
                    starting[key] += value
                }
            })
        })
        return starting
    }

    useEffect(() => {
        const getHand = (player) => {
            let handSlots = [];
            let style = {

            }
            if (player) {
                player.hand.forEach((card, index) => {
                    let indexedCard = {...card}
                    indexedCard['index'] = index
                    if (isAttached[index] === null || isAttached[index] === undefined) {
                        handSlots.push((<HandSlot
                            card={indexedCard} attached={using[index]} index={index} registerDrop={registerDrop}
                            registerHovered={props.registerHovered} />))
                    }
                })
            }
            return handSlots
        }

        let userPlayer = null
        gameState.players.forEach((player) => {
            if (authTokens.user.userId === player.userId) {
                userPlayer = player;
            }
        })
        setPlayer(userPlayer)
        const hand = getHand(userPlayer)
        setCardSlots(hand)
    }, [gameState.players, authTokens.user.userId, props.registerHovered, isAttached, using])

    const getHandValues = () => {
        let attributes = {
            goldValue: 0,
            light: 0,
            attack: 0,
            magicAttack: 0,
            buys: 0
        }
        if (player) {
            let generalEffects = []
            player.hand.forEach((card, index) => {
                generalEffects = generalEffects.concat(addGeneralEffects(card, index))
            })
            player.hand.forEach((card, index) => {
                let newAttributes = getAttributes(card, using[index], generalEffects)
                attributes = mergeObjects(attributes, newAttributes)
            })
        }
        player.hand.forEach((card, index) => {
            addLateEffects(card, index, attributes)
        })
        return attributes
    }

    if (player && player.hand.length > 0) {
        const { goldValue, light, attack, magicAttack } = getHandValues()
        return (
            <StatusContainer>
                <div>gold: {goldValue}, light: {light} attack: {attack}, magicAttack: {magicAttack}</div>
                <HandContainer>
                    {cardSlots.map((c, index) => <div key={index + 1}>{c}</div>)}
                </HandContainer>
            </StatusContainer>
        )


    } else {
        return <HandContainer />
    }
}

export default PlayerHand;