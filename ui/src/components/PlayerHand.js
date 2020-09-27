import React, { useState, useEffect } from "react";
import {useGameState} from "../context/GameState";
import { useAuth } from "../context/auth";
import cardImages from "../img/cards/cards"
import styled from "styled-components";
import HeroCard from "./HeroCard";
import WeaponCard from "./WeaponCard";
import ItemCard from "./ItemCard";
import FoodCard from "./FoodCard";
import SpellCard from "./SpellCard";
import VillagerCard from "./VillagerCard";
import MonsterCard from "./MonsterCard";
import {isGeneralEffect, executeEffect, isEarlyEffect, isLateEffect} from "../services/effects"

const StatusContainer = styled.div`
    display: flex;
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
    const [ cards, setCards ] = useState([])
    const [ attributes, setAttributes ] = useState([])

    const initialAttributes = (card) => {
        let starting = {
            goldValue: 0,
            light: 0,
            attack: 0,
            magicAttack: 0,
            strength: 0,
            weight: 0,
        }
        Object.keys(card.data).forEach((key) => {
            let value = card.data[key]
            if (key in starting) {
                starting[key] = value
            }
        })
        return starting
    }
    const getHand = (player) => {
        let handCards = [];
        let newAttributes = []
        if (player) {
            for (let index=0; index<player.hand.length; index++) {
                const card = player.hand[index]
                newAttributes.push(initialAttributes(card))

                switch (card.cardType) {
                    case "HeroCard":
                        handCards.push(<HeroCard card={card}/>)
                        break;
                    case "ItemCard":
                        if (card.data.traits.includes("Food")) {
                            handCards.push(<FoodCard card={card}/>)
                        } else {
                            handCards.push(<ItemCard card={card}/>)
                        }
                        break;
                    case "SpellCard":
                        handCards.push(<SpellCard card={card}/>)
                        break;
                    case "VillagerCard":
                        handCards.push(<VillagerCard card={card}/>)
                        break
                    case "WeaponCard":
                        handCards.push(<WeaponCard card={card}/>)
                        break;
                    case "MonsterCard":
                        handCards.push(<MonsterCard card={card}/>)
                }
            }
            setCards(handCards)
            setAttributes(newAttributes)
        }
        return {hand: handCards, attributes: newAttributes}
    }

    const addGeneralEffects = (card) => {
        let generalEffects = []
        if (card.data.dungeonEffects) {
            card.data.dungeonEffects.map((effect) => {
                if (isGeneralEffect(effect)) {
                    generalEffects.push(effect)
                }
            })
        }
        if (card.data.villageEffects) {
            card.data.villageEffects.map((effect) => {
                if (isGeneralEffect(effect)) {
                    generalEffects.push(effect)
                }
            })
        }
        return generalEffects
    }

    const addInitialEffects = (card, index, generalEffects, currentAttributes) => {
        let newCardAttributes = currentAttributes[index]
        generalEffects.map((effect) => {
            newCardAttributes = executeEffect(effect, newCardAttributes)
        })
        if (card.data.dungeonEffects) {
            card.data.dungeonEffects.map((effect) => {
                if (isEarlyEffect(effect)) {
                    newCardAttributes = executeEffect(effect, newCardAttributes)
                }
            })
        }
        if (card.data.villageEffects) {
            card.data.villageEffects.map((effect) => {
                if (isEarlyEffect(effect)) {
                    newCardAttributes = executeEffect(effect, newCardAttributes)
                }
            })
        }
        let newAttributes = currentAttributes
        newAttributes[index] = newCardAttributes
        return newAttributes
    }

    const addLateEffects = (card, index, currentAttributes) => {
        let newAttributes = [...currentAttributes]
        let newCardAttributes = newAttributes[index]
        if (card.data.dungeonEffects) {
            card.data.dungeonEffects.map((effect) => {
                if (isLateEffect(effect)) {
                    newCardAttributes = executeEffect(effect, newCardAttributes)
                }
            })
        }
        if (card.data.villageEffects) {
            card.data.villageEffects.map((effect) => {
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
        attributes.forEach((cardAttributes) => {
            Object.keys(cardAttributes).forEach((key) => {
                let value = cardAttributes[key]
                if (key in starting) {
                    starting[key] += value
                }
            })
        })
        return starting
    }
    const getHandValues = (hand, currentAttributes) => {
        let generalEffects = []
        let newAttributes = [...currentAttributes]
        hand.map((card, index) => {
            if (!attached[index] && card.props.card.cardType !== "WeaponCard") {
                generalEffects.concat(addGeneralEffects(card.props.card, index))
                if (using[index]) {
                    for (let i=0; i<using[index]; i++) {
                        generalEffects.concat(addGeneralEffects(using[index][i].props.card, index))
                    }
                }
            }
        })
        hand.map((card, index) => {
            if (!attached[index] && card.props.card.cardType !== "WeaponCard") {
                newAttributes = addInitialEffects(card.props.card, index, generalEffects, newAttributes)
                if (using[index]) {
                    for (let i=0; i<using[index]; i++) {
                        addInitialEffects(using[index][i].props.card, index, generalEffects, newAttributes)
                    }
                }
            }
        })
        hand.map((card, index) => {
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

    useEffect(() => {
        let userPlayer = null
        gameState.players.map((player) => {
            if (authTokens.user.userId === player.userId) {
                userPlayer = player;
            }
        })
        setPlayer(userPlayer)
        const { hand, attributes } = getHand(userPlayer)
        if (hand) {
            getHandValues(hand, attributes)
        }
    }, [gameState.players, authTokens.user.userId])

    if (player && player.hand.length > 0) {
        const { goldValue, light, attack, magicAttack } = sumAttributes()
        return (
            <StatusContainer>
                <div>gold: {goldValue}, light: {light} attack: {attack}, magicAttack: {magicAttack}</div>
                <HandContainer>
                    {cards.map((c, index) => <div>{attached[index] || c}</div>)}
                </HandContainer>
            </StatusContainer>
        )


    } else {
        return <HandContainer />
    }
}

export default PlayerHand;