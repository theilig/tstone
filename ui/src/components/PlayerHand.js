import React, { useState, useEffect } from "react";
import {useGameState} from "../context/GameState";
import { useAuth } from "../context/auth";
import styled from "styled-components";
import WeaponCard from "./WeaponCard";

import {isGeneralEffect, executeEffect, isEarlyEffect, isLateEffect} from "../services/effects"
import {getCardInfo} from "./HandCard";
import HandSlot from "./HandSlot";

const StatusContainer = styled.div`
    display: flex;ƒ
    flex-direction: column;
`;

const HandContainer = styled.div`
    display: flex;
    flex-direction: row;
    margin-bottom: 20px;
`;

const HandCard = styled.img`
    width: 126px;
    height: 180px;
    margin-left: 10px;
`;


function PlayerHand(props) {
    const { gameState } = useGameState()
    const { authTokens } = useAuth()
    const [ player, setPlayer ] = useState(null)
    const [ attached, setAttached ] = useState([])
    const [ using, setUsing ] = useState([])
    const [ cardSlots, setCardSlots ] = useState([])
    const [ attributes, setAttributes ] = useState({})

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
        let newAttributes = [...currentAttributes]
        let newCardAttributes = newAttributes[index]
        if (card.data.dungeonEffects) {
            card.data.dungeonEffects.forEach((effect) => {
                if (isLateEffect(effect)) {
                    newCardAttributes = executeEffect(effect, newCardAttributes)
                }
            })
        }
        if (card.data.villageEffects) {
            card.data.villageEffects.forEach((effect) => {
                if (isLateEffect(effect)) {
                    newCardAttributes = executeEffect(effect, newCardAttributes)
                }
            })
        }
        newAttributes[index] = newCardAttributes
        return newAttributes
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

    const setAttributesForSlot = (cardAttributes, index) => {
        let newAttributes = {...attributes}
        newAttributes[index] = cardAttributes
        setAttributes(newAttributes)
    }

    useEffect(() => {
        const getHand = (player) => {
            let handSlots = [];
            let generalEffects = []
            if (player) {
                player.hand.forEach((card, index) => {
                    generalEffects.concat(addGeneralEffects(card, index))
                })
                player.hand.forEach((card, index) => {
                    if (!attached[index]) {
                        handSlots.push((<HandSlot
                            card={card} attached={using[index] ?? []} index={index} generalEffects={generalEffects}
                            setAttributes={(attributes, index) => setAttributesForSlot(attributes, index)}
                        registerHovered={props.registerHovered} />))
                    }
                })
                setCardSlots(handSlots)
            }
            return handSlots
        }

        const getHandValues = (hand, currentAttributes) => {
            let newAttributes = [...currentAttributes]
            hand.forEach((card, index) => {
                if (!attached[index] && card.props.card.cardType !== "WeaponCard") {
                    addLateEffects(card.props.card, index, newAttributes)
                    if (using[index]) {
                        for (let i=0; i<using[index]; i++) {
                            addLateEffects(using[index][i].props.card, index, newAttributes)
                        }
                    }
                }
            })
            setAttributes(newAttributes)
        }

        let userPlayer = null
        gameState.players.forEach((player) => {
            if (authTokens.user.userId === player.userId) {
                userPlayer = player;
            }
        })
        setPlayer(userPlayer)
        const { hand, attributes } = getHand(userPlayer)
        if (hand) {
            getHandValues(hand, attributes)
        }
    }, [gameState.players, authTokens.user.userId, props.registerHovered, attached, using])

    if (player && player.hand.length > 0) {
        const { goldValue, light, attack, magicAttack } = sumAttributes()
        return (
            <StatusContainer>
                <div>gold: {goldValue}, light: {light} attack: {attack}, magicAttack: {magicAttack}</div>
                <HandContainer>
                    {cardSlots.map((c, index) => <div key={index + 1}>{attached[index] || c}</div>)}
                </HandContainer>
            </StatusContainer>
        )


    } else {
        return <HandContainer />
    }
}

export default PlayerHand;