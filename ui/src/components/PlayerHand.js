import React, { useState, useEffect } from "react";
import {useGameState} from "../context/GameState";
import { useAuth } from "../context/auth";
import styled from "styled-components";

import {isGeneralEffect, executeEffect, isLateEffect} from "../services/effects"
import HandSlot from "./HandSlot";
import {getAttributes} from "./HandCard";
import DestroySlot from "./DestroySlot";
import AttributeValues from "./AttributeValues";

const HandContainer = styled.div`
    display: flex;
    flex-direction: row;
    margin-bottom: 20px;
`;

const StatusContainer = styled.div`
    display: flex;
    flex-direction: column;
`;

export const DISCARD_INDEX = "DISCARD";
export const DESTROY_INDEX = "DESTROY";

function PlayerHand(props) {
    const { gameState } = useGameState()
    const { authTokens } = useAuth()
    const [ player, setPlayer ] = useState(null)
    const [ isAttached, setIsAttached ] = useState([])
    const [ using, setUsing ] = useState([])
    const [ arrangement, setArrangement ] = useState([])

    const registerDrop = (source, target) => {
        let newAttached = [...isAttached]
        let newUsing = {...using}
        if (newAttached[source] != null) {
            let removeUsing = [...using[newAttached[source]]]
            removeUsing = removeUsing.filter((c) => {
                return c.index !== source
            })
            newUsing[newAttached[source]] = removeUsing
            newAttached[source] = null
        }
        // Only allow one card in discard or destroy slot by freeing up any existing card in there
        if (newUsing[target] && (target === DISCARD_INDEX || target === DESTROY_INDEX)) {
            newUsing[target].forEach(c => {
                newAttached[c.index] = null
            })
            newUsing[target] = []
        }
        if (target != null) {
            newAttached[source] = target
            let usingList = newUsing[target] ?? []
            let indexedCard = {...player.hand[source]}
            indexedCard['index'] = source
            usingList.push(indexedCard)
            newUsing[target] = usingList
        }
        if (target === DESTROY_INDEX) {
            props.registerDestroy(player.hand[source].data.name)
        }
        setUsing(newUsing)
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

    useEffect(() => {
        const getHand = (player) => {
            let cardArrangement = []
            if (player) {
                player.hand.forEach((card, index) => {
                    let indexedCard = {...card}
                    indexedCard['index'] = index
                    if (isAttached[index] == null) {
                        let arrangementIndex = cardArrangement.length
                        cardArrangement.push([indexedCard])
                        if (using[index] != null) {
                            using[index].forEach((card) => {
                                cardArrangement[arrangementIndex].push(card)
                            })
                        }
                    }
                })
                if (using[DISCARD_INDEX]) {
                    cardArrangement[DISCARD_INDEX] = using[DISCARD_INDEX]
                }
                if (using[DESTROY_INDEX]) {
                    cardArrangement[DESTROY_INDEX] = using[DESTROY_INDEX]
                }
                setArrangement(cardArrangement)
            }
        }

        let userPlayer = null
        gameState.players.forEach((player) => {
            if (authTokens.user.userId === player.userId) {
                userPlayer = player;
            }
        })
        setPlayer(userPlayer)
        getHand(userPlayer)
    }, [gameState.players, authTokens.user.userId, props.registerHovered, isAttached, using])

    const getHandValues = () => {
        let attributes = {
            goldValue: 0,
            light: 0,
            attack: 0,
            magicAttack: 0,
            buys: 1
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
        getHandValues()
        return (
            <StatusContainer>
                <AttributeValues show={props.show} values={getHandValues()} />
                <HandContainer>
                    {props.registerDestroy && <DestroySlot registerDrop={registerDrop}
                                                           registerHovered={props.registerHovered}
                                                           cards={arrangement[DESTROY_INDEX] ?? []}
                                                           key={DESTROY_INDEX} index={DESTROY_INDEX}/>}
                    {arrangement.map((cardList, index) => {
                        return (<HandSlot cards={cardList} index={index} key={index} registerDrop={registerDrop}
                            registerHovered={props.registerHovered} />)
                    })}
                </HandContainer>
            </StatusContainer>
        )


    } else {
        return <HandContainer />
    }
}

export default PlayerHand;