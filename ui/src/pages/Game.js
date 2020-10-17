import React, {useState, useEffect} from "react";
import { useAuth } from "../context/auth";
import { GameStateContext } from "../context/GameState";
import {useParams} from "react-router";
import Startup from "./Startup";
import {Redirect} from "react-router";
import ChoosingDestination from "./ChoosingDestination";
import cardImages from "../img/cards/cards";
import Resting from "./Resting";
import {DndProvider} from "react-dnd";
import {HTML5Backend} from "react-dnd-html5-backend";
import {executeEffect, isGeneralEffect, isLateEffect} from "../services/effects";
import {getAttributes, upgradeCost} from "../components/HandCard";
import Purchasing from "./Purchasing";
import Crawling from "./Crawling";
import {DESTROY_OFFSET} from "../components/DestroySlot";
import {UPGRADE_OFFSET} from "../components/UpgradeSlot";
import Upgrading from "./Upgrading";

function Game() {
    const [ gameState, setGameState ] = useState()
    const [ gameSocket, setGameSocket ] = useState(null)
    const { authTokens } = useAuth()
    const [ gameOver, setGameOver] = useState(false)
    const [ hovered, setHovered] = useState(null)
    const [ isAttached, setIsAttached ] = useState({})
    const [ using, setUsing ] = useState([])
    const [ player, setPlayer ] = useState(null)

    let game = useParams()
    let gameId = parseInt(game.gameId)

    const setState = (data) => {
        setGameState(data);
    }

    useEffect(() => {
        if (gameSocket === null) {
            let ws = new WebSocket('ws://localhost:9000/api/game')
            setGameSocket(ws)
            ws.onopen = () => {
                ws.send(JSON.stringify(
                    {
                        messageType: "Authentication",
                        data: {
                            token: authTokens.token
                        }
                    }
                ))
                ws.send(JSON.stringify(
                    {
                        messageType: "ConnectToGame",
                        data: {
                            gameId: gameId
                        }
                    }
                ))
            }
            ws.onmessage = evt => {
                // listen to data sent from the websocket server
                const message = JSON.parse(evt.data)
                if (message.messageType === "GameState") {
                    setState(message.data.state)
                } else if (message.messageType === "GameOver") {
                    setGameOver(true)
                }
                console.log(message)
            }
            ws.onclose = () => {
                console.log('disconnected')
                // automatically try to reconnect on connection loss
            }
        }
        if (gameState) {
            gameState.players.forEach((p) => {
                if (authTokens.user.userId === p.userId) {
                    setPlayer(p)
                }
            })
        }

    }, [gameSocket, authTokens.token, gameId, gameState])

    const registerDrop = (source, target) => {
        let newAttached = {...isAttached}
        let newUsing = [...using]
        if (newAttached[source] != null) {
            let removeUsing = [...using[newAttached[source]]]
            removeUsing = removeUsing.filter((c) => {
                return c.index !== source
            })
            newUsing[newAttached[source]] = removeUsing
            newAttached[source] = null
        }
        if (target != null) {
            newAttached[source] = target
            let usingList = newUsing[target] ?? []
            let indexedCard = {...player.hand[source]}
            indexedCard['index'] = source
            usingList.push(indexedCard)
            newUsing[target] = usingList
        }

        setUsing(newUsing)
        setIsAttached(newAttached)
    }

    const canUpgrade = (card, xp) => {
        const cost = upgradeCost(card)
        return cost > 0 && xp >= cost
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

    const getHandValues = (arrangement, isVillage) => {
        let attributes = {
            goldValue: 0,
            light: 0,
            attack: 0,
            magicAttack: 0,
            buys: 1,
            experience: 0
        }

        let generalEffects = []
        arrangement.forEach((column) => {
            column[0].forEach(card => {
                generalEffects = generalEffects.concat(addGeneralEffects(card))
            })
        })
        arrangement.forEach((column) => {
            const newAttributes = getAttributes(column, generalEffects, isVillage)
            attributes = mergeObjects(attributes, newAttributes)
        })

        arrangement.forEach((column, index) => {
            column[0].forEach(card => {
                attributes = addLateEffects(card, index, attributes)
            })
        })
        return attributes
    }

    const getArrangement = (player, stage) => {
        const hand = player.hand
        const xp = player.xp
        const canDestroy = (effects) => {
            if (effects) {
                let result = false
                effects.forEach(e => {
                    if (e.effect === "Destroy") {
                        result = true;
                    }
                })
                return result
            }
            return false
        }
        let cardArrangement = []
        const addDestroy = (c) => {
            if (stage === "Purchasing" && canDestroy(c.data.villageEffects)) {
                return true
            }
            if (stage === "Crawling" && canDestroy(c.data.dungeonEffects)) {
                return true
            }
        }
        hand.forEach((card, index) => {
            let indexedCard = {...card}
            indexedCard['index'] = index
            if (isAttached[index] == null || using[DESTROY_OFFSET + index] != null) {
                let arrangementIndex = cardArrangement.length
                cardArrangement.push([[indexedCard]])
                let destroySlot = (stage === "Resting" && arrangementIndex === 0) || using[DESTROY_OFFSET] != null
                const upgradeSlot = (stage === "Upgrading" && canUpgrade(card, xp))
                if (card.cardType !== "WeaponCard") {
                    destroySlot = destroySlot || addDestroy(card)
                }
                if (using[index] != null) {
                    using[index].forEach((card) => {
                        cardArrangement[arrangementIndex][0].push(card)
                        destroySlot = destroySlot || addDestroy(card)
                    })
                }
                if (upgradeSlot) {
                    cardArrangement[arrangementIndex][1] = using[UPGRADE_OFFSET + index] ?? []
                }
                if (destroySlot) {
                    cardArrangement[arrangementIndex][1] = using[DESTROY_OFFSET + index] ?? []
                }
            }
        })
        return cardArrangement
    }

    const renderGameStage = () => {
        if (!gameState) {
            return ""
        }
        let arrangement = []
        let stage = gameState.currentStage

        let attributes = {}
        let upgrading = []
        const activePlayer = stage.stage && stage.data && stage.data.currentPlayerId != null &&
            parseInt(authTokens.user.userId) === stage.data.currentPlayerId
        if (player && player.hand.length > 0) {
            arrangement = getArrangement(player, activePlayer ? stage.stage : "ChoosingDestination")
            if (stage.stage === "Upgrading") {
                upgrading = player.hand.filter(c => canUpgrade(c, player.xp)).map(c => c.data.name)
                attributes = {experience: player.xp + arrangement.map(columns => {
                        if (columns[1] && columns[1][1]) {
                            return upgradeCost(columns[1][1])
                        } else {
                            return 0
                        }
                    }).reduce((total, extra) => total + extra)}
            } else {
                attributes = getHandValues(arrangement, activePlayer ? stage.stage === "Purchasing" : false)
                attributes.experience = attributes.experience + player.xp
            }
        }

        if (activePlayer) {
            switch (stage.stage) {
                case "ChoosingDestination":
                    return <ChoosingDestination registerHovered={registerHovered} renderHovered={renderHovered}
                                                registerDrop={registerDrop} attributes={attributes}
                                                gameSocket={gameSocket} arrangement={arrangement} />
                case "Resting":
                    return <Resting registerHovered={registerHovered} renderHovered={renderHovered}
                                    registerDrop={registerDrop}
                                    gameSocket={gameSocket} arrangement={arrangement} />
                case "Purchasing":
                    return <Purchasing registerHovered={registerHovered} renderHovered={renderHovered}
                                       registerDrop={registerDrop} attributes={attributes}
                                       gameSocket={gameSocket} arrangement={arrangement} />
                case "Crawling":
                    return <Crawling registerHovered={registerHovered} renderHovered={renderHovered}
                                     registerDrop={registerDrop} attributes={attributes}
                                     gameSocket={gameSocket} arrangement={arrangement} />
                case "Upgrading":
                    return <Upgrading registerHovered={registerHovered} renderHovered={renderHovered}
                                      registerDrop={registerDrop} attributes={attributes}
                                      gameSocket={gameSocket} arrangement={arrangement}
                                      upgrading={upgrading} />
            }
        } else {
            switch (stage.stage) {
                case "WaitingForPlayers":
                    return <Startup gameSocket={gameSocket} />
                default:
                    return <ChoosingDestination registerHovered={registerHovered} renderHovered={renderHovered}
                                                registerDrop={registerDrop} attributes={attributes}
                                                gameSocket={gameSocket} arrangement={arrangement} />
            }
        }
    }

    const registerHovered = (name, location) => {
        if (name && name !== "CardBack") {
            setHovered({name:name, location:location})
        } else {
            setHovered(null)
        }
    }

    const renderHovered = () => {
        if (hovered) {
            const imgStyle = {
                width: '300px',
                height: '375px',
                position: 'absolute',
                top: hovered.location.top,
                left: hovered.location.left,
                pointerEvents: 'none'
            };
            return (<img style={imgStyle} src={cardImages[hovered.name]} title={hovered.name} alt={hovered.name} />)
        }
    }

    if (gameOver) {
        if (gameSocket !== null) {
            setGameSocket(null)
            gameSocket.close()
        }
        return (
            <Redirect to={"/"} />
        )
    }
    return (
        <GameStateContext.Provider value={{ gameState, setGameState: setState  }}>
            <DndProvider backend={HTML5Backend}>
                {renderGameStage()}
            </DndProvider>
        </GameStateContext.Provider>
    )
}

export default Game;
