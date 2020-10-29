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
import Upgrading from "./Upgrading";
import {SourceIndexes, TargetIndexes} from "../components/SlotIndexes";
import {villageCategories} from "../components/Village";
import Destroying from "./Destroying";
import TakingSpoils from "./TakingSpoils";
import Discarding from "./Discarding";
import GameResult from "./GameResult";
import {serializeArrangement} from "../services/Arrangement";

function Game() {
    const [ gameState, setGameState ] = useState()
    const [ gameSocket, setGameSocket ] = useState(null)
    const { authTokens } = useAuth()
    const [ gameOver, setGameOver] = useState(false)
    const [ hovered, setHovered] = useState(null)
    const [ isAttached, setIsAttached ] = useState({})
    const [ using, setUsing ] = useState({})
    const [ player, setPlayer ] = useState(null)
    const [ remoteAttributes, setRemoteAttributes ] = useState({})

    let game = useParams()
    let gameId = parseInt(game.gameId)

    const setState = (data) => {
        if (data.currentStage.stage === "ChoosingDestination") {
            setIsAttached({})
            setUsing({})
        }
        data.players.forEach(p => {
            if (authTokens.user.userId === p.userId && p.attributes) {
                setRemoteAttributes(p.attributes)
            }
        })
        setGameState(data);
    }

    const cardBack = {
        cardType: 'CardBack',
        data: {
            id: 0,
            name: "CardBack",
            imageName: "card000.png",
            frequency: 0
        }
    }

    const connectToGame = (ws, gameId) => {
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

    const openSocket = () => {
        const indexCards = (state => {
            if (state.currentStage.stage === "WaitingForPlayers" ||
                state.currentStage.stage === "GameEnded")  {
                return state
            }
            state.players.forEach((p, index) => {
                if (p.hand) {
                    p.hand.forEach((c, cardIndex) => {
                        c.data.sourceIndex = SourceIndexes.HandIndex + SourceIndexes.HandOffset * cardIndex +
                            SourceIndexes.PlayerOffset * index
                    })
                }
            })
            let villageIndex = 0
            villageCategories.forEach(category => {
                state.village[category].forEach(pile => {
                    pile.cards.forEach((c, index) => {
                        c.sourceIndex = SourceIndexes.VillageIndex + SourceIndexes.VillagePileOffset * villageIndex +
                            index
                    })
                    villageIndex += 1
                })
            })
            state.dungeon.ranks.concat(state.dungeon.monsterPile).forEach((c, index) => {
                c.data.sourceIndex = SourceIndexes.DungeonIndex + index
            })
            state.players.forEach((p) => {
                if (authTokens.user.userId === p.userId) {
                    setPlayer(p)
                }
            })

            return state
        })

        let ws = new WebSocket('ws://localhost:9000/api/game')
        setGameSocket(ws)
        ws.onopen = () => {
            connectToGame(ws, gameId)
        }
        ws.onmessage = evt => {
            // listen to data sent from the websocket server
            const message = JSON.parse(evt.data)
            if (message.messageType === "GameState") {
                setState(indexCards(message.data.state))
            } else if (message.messageType === "GameOver") {
                setGameOver(true)
            } else if (message.messageType === "AttributeResult") {
                setRemoteAttributes(message.data.attributes)
            } else if (message.messageType === "Not Authenticated") {
                connectToGame(ws, gameId)
            }
            console.log(message)
        }
        ws.onclose = () => {
            console.log('disconnected')
            // automatically try to reconnect on connection loss
        }
        return ws
    }

    const sendMessage = (message) => {
        let ws = gameSocket
        if (!gameSocket || gameSocket.readyState === WebSocket.CLOSED) {
            ws = openSocket()
        }
        ws.send(message)
    }

    useEffect(() => {
        if (gameSocket == null) {
            openSocket()
        }
    }, [openSocket])

    const updateAttributes = (currentAttached, currentUsing) => {
        let activePlayer = isActivePlayer(gameState)
        const arrangement =
            getArrangement(
                player,
                activePlayer ? gameState.currentStage : {stage: "ChoosingDestination"},
                currentAttached,
                currentUsing
            )

        let finalArrangement = serializeArrangement(arrangement)
        gameSocket.send(JSON.stringify(
            {
                messageType: "GetAttributes",
                data: {
                    gameId: gameState.gameId,
                    arrangement: finalArrangement
                }
            }
        ))
    }
    const isActivePlayer = (state) => {
        if (state && state.currentStage) {
            const stage = state.currentStage
            return stage.stage && stage.data &&
                stage.data.currentPlayerId != null &&
                parseInt(authTokens.user.userId) === stage.data.currentPlayerId
        } else return false
    }

    const isDungeonUsing = (index) => {
        return (using[TargetIndexes.DungeonIndex + index] ?? []).length > 0
    }

    const haveSentToBottom = () => {
        return isDungeonUsing(gameState.dungeon.ranks.length)
    }

    const haveBanished = () => {
        let banished = false
        for (let i = 0; i < gameState.dungeon.ranks.length; i++) {
            banished = banished || isDungeonUsing(i)
        }
        return banished
    }

    const setDungeonCards = (newAttached, newUsing) => {
        const dungeonCards = []
        const numRanks = gameState.dungeon.ranks.length
        for (let i = 0; i < numRanks; i++) {
            if ((newUsing[TargetIndexes.DungeonIndex + i] ?? []).length > 0) {
                dungeonCards.push(newUsing[TargetIndexes.DungeonIndex + i][0])
            } else if (newAttached[SourceIndexes.DungeonIndex + i]) {
                dungeonCards.push(cardBack)
            } else {
                dungeonCards.push(gameState.dungeon.ranks[i])
            }
        }
        if ((newUsing[TargetIndexes.DungeonIndex + numRanks] ?? []).length > 0) {
            dungeonCards.push(newUsing[TargetIndexes.DungeonIndex + numRanks][0])
        } else {
            dungeonCards.push(cardBack)
        }
        let newGameState = {...gameState}
        newGameState.dungeonCards = dungeonCards
        setGameState(newGameState)
    }
    const resetDungeon = (newAttached, newUsing) => {
        for (let i = 0; i <= gameState.dungeon.ranks.length; i++) {
            delete newAttached[SourceIndexes.DungeonIndex + i]
            newUsing[TargetIndexes.DungeonIndex + i] = []
        }
        return rearrangeDungeon(newAttached, newUsing)
    }

    const rearrangeDungeon = (newAttached, newUsing) => {
        let finalCards = []
        let insertBack = (newUsing[TargetIndexes.DungeonIndex + gameState.dungeon.ranks.length] ?? []).length > 0
        for (let i = 0; i <= gameState.dungeon.ranks.length; i++) {
            const targetIndex = TargetIndexes.DungeonIndex + i
            let insertedUnattached = false
            if (i > finalCards.length && newAttached[SourceIndexes.DungeonIndex + i] == null) {
                if (gameState.dungeon.ranks[i] != null) {
                    finalCards.push(gameState.dungeon.ranks[i])
                }
                insertedUnattached = true
            }
            if (newUsing[targetIndex] != null) {
                if (i > finalCards.length) {
                    for (let j = 0; j < newUsing[targetIndex].length; j++) {
                        finalCards.push(newUsing[targetIndex][j])
                    }
                } else {
                    for (let j = newUsing[targetIndex].length - 1; j >= 0; j--) {
                        finalCards.push(newUsing[targetIndex][j])
                    }
                }
            }
            if (!insertedUnattached && i <= finalCards.length && newAttached[SourceIndexes.DungeonIndex + i] == null) {
                if (gameState.dungeon.ranks[i] != null) {
                    finalCards.push(gameState.dungeon.ranks[i])
                }
            }
        }
        if (insertBack) {
            finalCards.splice(2, 0, cardBack)
        }

        let finalAttached = {}
        let finalUsing = {}
        finalCards.slice(0, 4).forEach((card, index) => {
            if (card.cardType !== 'CardBack' && card.data.sourceIndex !== SourceIndexes.DungeonIndex + index) {
                finalUsing[TargetIndexes.DungeonIndex + index] = [card]
                finalAttached[card.data.sourceIndex] = TargetIndexes.DungeonIndex + index
            }
        })
        return {dungeonAttached: finalAttached, dungeonUsing: finalUsing}
    }

    const registerDrop = (source, targetIndex) => {
        const sourceIndex = source.data.sourceIndex
        let newAttached = {...isAttached}
        let newUsing = {...using}
        if (newAttached[sourceIndex] != null) {
            let removeUsing = [...using[newAttached[sourceIndex]]]
            removeUsing = removeUsing.filter((c) => {
                return c.data.sourceIndex !== sourceIndex
            })
            newUsing[newAttached[sourceIndex]] = removeUsing
            newAttached[sourceIndex] = null
        }
        if (targetIndex != null) {
            newAttached[sourceIndex] = targetIndex
            let usingList = newUsing[targetIndex] ?? []
            usingList.push(source)
            newUsing[targetIndex] = usingList
        }
        if (targetIndex - targetIndex % 1000 === TargetIndexes.DungeonIndex) {
            if (targetIndex === TargetIndexes.DungeonIndex + gameState.dungeon.ranks.length) {
                // Sending to the bottom, reset any other re-arrangement
                for (let i = 0; i < gameState.dungeon.ranks.length; i++) {
                    if (newUsing[TargetIndexes.DungeonIndex + i] != null) {
                        newUsing[TargetIndexes.DungeonIndex + i].forEach((card) => {
                            newAttached[card.data.sourceIndex] = null
                        })
                        newUsing[TargetIndexes.DungeonIndex + i] = []
                    }
                }
            } else {
                // Re-ordering remove anything sent to bottom
                if (newUsing[TargetIndexes.DungeonIndex + gameState.dungeon.ranks.length] != null) {
                    newUsing[TargetIndexes.DungeonIndex + gameState.dungeon.ranks.length].forEach((card) => {
                        newAttached[card.data.sourceIndex] = null
                    })
                    newUsing[TargetIndexes.DungeonIndex + gameState.dungeon.ranks.length] = []
                }
            }
            const { dungeonAttached, dungeonUsing } = rearrangeDungeon(newAttached, newUsing)
            setUsing(dungeonUsing)
            setIsAttached(dungeonAttached)
            setDungeonCards(dungeonAttached, dungeonUsing)
        } else if (source.data.sourceIndex - source.data.sourceIndex % 1000 === SourceIndexes.DungeonIndex &&
            targetIndex == null) {
            const { dungeonAttached, dungeonUsing } = resetDungeon(newAttached, newUsing)
            setUsing(dungeonUsing)
            setIsAttached(dungeonAttached)
            setDungeonCards(dungeonAttached, dungeonUsing)
        } else {
            setUsing(newUsing)
            setIsAttached(newAttached)
        }
        updateAttributes(newAttached, newUsing)
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

        if (gameState.currentStage && gameState.currentStage.data) {
            attributes["banishes"] = -(gameState.currentStage.data.banishes ?? 0)
            attributes["sendToBottoms"] = -(gameState.currentStage.data.sendToBottoms ?? 0)
        } else {
            attributes["banishes"] = 0
            attributes["sendToBottom"] = 0
        }

        let generalEffects = []
        arrangement.forEach((column) => {
            column.cards.forEach(card => {
                generalEffects = generalEffects.concat(addGeneralEffects(card))
            })
        })
        arrangement.forEach((column) => {
            const newAttributes = getAttributes(column, generalEffects, isVillage)
            attributes = mergeObjects(attributes, newAttributes)
        })

        arrangement.forEach((column, index) => {
            column.cards.forEach(card => {
                attributes = addLateEffects(card, index, attributes)
            })
        })
        return attributes
    }

    const addBanish = (stage, c) => {
        if (stage === "Crawling" && canBanish(c.data.dungeonEffects)) {
            return true
        }
        if (stage === "TakingSpoils" && canBanish(c.data.battleEffects)) {
            return true
        }
    }
    const canBanish = (effects) => {
        if (effects) {
            let result = false
            effects.forEach(e => {
                if (e.effect === "SendToBottom") {
                    result = true
                }
            })
            return result
        }
        return false
    }

    const getArrangement = (player, stage, currentAttached, currentUsing) => {
        const hand = player.hand
        const xp = player.xp
        const canDestroy = (effects) => {
            if (effects) {
                let result = false
                effects.forEach(e => {
                    if (e.effect === "Destroy") {
                        result = true
                    } else if (e.effect === "Banish" && (haveBanished() || haveSentToBottom())) {
                        result = true
                    }
                })
                return result
            }
            return false
        }
        let cardArrangement = []
        const addDestroy = (c, includeEffects) => {
            if (includeEffects && stage.stage === "Purchasing" && canDestroy(c.data.villageEffects)) {
                return true
            }
            if (includeEffects && stage.stage === "Crawling" && canDestroy(c.data.dungeonEffects)) {
                return true
            }
            if (stage.stage === "Destroying") {
                const targets = stage.data.possibleCards.map(c => c.data.name)
                return targets.includes(c.data.name)
            }
            if (stage.stage === "DiscardOrDestroy") {
                return true
            }
        }
        let playerDiscarding = stage.stage === "PlayerDiscard" && stage.data.howMany > stage.data.borrows &&
            stage.data.playerIds.includes(player.userId)
        let playerLoaning = stage.stage === "PlayerDiscard" && stage.data.borrows > 0 &&
            stage.data.playerIds.includes(player.userId)
        hand.forEach(card => {
            let destroyIndex = TargetIndexes.DestroyIndex - TargetIndexes.HandIndex + card.data.sourceIndex
            if (currentAttached[card.data.sourceIndex] == null || currentUsing[destroyIndex] != null) {
                let arrangementIndex = cardArrangement.length
                cardArrangement.push({cards: [card]})
                let destroySlot = (stage.stage === "Resting" && arrangementIndex === 0) ||
                    (currentUsing[destroyIndex] != null && currentUsing[destroyIndex].length > 0)
                const upgradeSlot = (stage.stage === "Upgrading" && canUpgrade(card, xp))
                const banishSlot = addBanish(stage.stage, card)
                const discardSlot = playerDiscarding && (
                    (arrangementIndex === 0 && !playerLoaning) ||
                    (arrangementIndex === 1 && playerLoaning)
                )
                const loanSlot = (arrangementIndex === 0 && playerLoaning)
                destroySlot = destroySlot || addDestroy(card, card.cardType !== "WeaponCard")

                if (currentUsing[card.data.sourceIndex] != null) {
                    currentUsing[card.data.sourceIndex].forEach(card => {
                        cardArrangement[arrangementIndex].cards.push(card)
                        destroySlot = destroySlot || addDestroy(card, true)
                    })
                }
                if (upgradeSlot) {
                    cardArrangement[arrangementIndex].upgrade = currentUsing[TargetIndexes.UpgradeIndex -
                    TargetIndexes.HandIndex + card.data.sourceIndex] ?? []
                }
                if (destroySlot) {
                    cardArrangement[arrangementIndex].destroyed = currentUsing[TargetIndexes.DestroyIndex +
                        card.data.sourceIndex - TargetIndexes.HandIndex] ?? []
                }
                if (banishSlot) {
                    cardArrangement[arrangementIndex].banish = currentUsing[TargetIndexes.BanishIndex +
                        card.data.sourceIndex - TargetIndexes.HandIndex] ?? []
                }
                if (discardSlot) {
                    cardArrangement[arrangementIndex].discard = currentUsing[TargetIndexes.DiscardIndex] ?? []
                }
                if (loanSlot) {
                    cardArrangement[arrangementIndex].loan = currentUsing[TargetIndexes.LoanIndex] ?? []
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
        let activePlayer = isActivePlayer(gameState)
        if (player && player.hand.length > 0) {
            arrangement = getArrangement(
                player,
                (activePlayer || stage.stage === "PlayerDiscard") ? stage : {stage: "ChoosingDestination"},
                isAttached,
                using)
            if (stage.stage === "Upgrading") {
                upgrading = player.hand.filter(c => canUpgrade(c, player.xp)).map(c => c.data.name)
                attributes = {experience: player.xp - arrangement.map(columns => {
                        if (columns.upgrade && columns.upgrade[1]) {
                            return upgradeCost(columns.upgrade[1])
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

                case "DiscardOrDestroy":
                case "Destroying":
                    return <Destroying registerHovered={registerHovered} renderHovered={renderHovered}
                                       registerDrop={registerDrop} attributes={attributes}
                                       gameSocket={gameSocket} arrangement={arrangement}
                    />
                case "TakingSpoils":
                    return <TakingSpoils registerHovered={registerHovered} renderHovered={renderHovered}
                                       registerDrop={registerDrop} attributes={attributes}
                                       spoils={stage.data.spoilsTypes.filter(s => s !== "DiscardOrDestroy")}
                                       gameSocket={gameSocket} arrangement={arrangement} />

                case "PlayerDiscard":
                    return <Discarding registerHovered={registerHovered} renderHovered={renderHovered}
                                         registerDrop={registerDrop} attributes={attributes}
                                         gameSocket={gameSocket} arrangement={arrangement} />
                default:
                    return <div>Unknown state</div>

            }
        } else {
            switch (stage.stage) {
                case "WaitingForPlayers":
                    return <Startup gameSocket={gameSocket} />
                case "PlayerDiscard":
                    let needDiscards = false
                    stage.data.playerIds.forEach(p => {
                        if (p === parseInt(authTokens.user.userId)) {
                            needDiscards = true
                        }
                    })
                    if (needDiscards) {
                        return (<Discarding
                            registerHovered={registerHovered} renderHovered={renderHovered}
                            registerDrop={registerDrop} attributes={attributes}
                            gameSocket={gameSocket} arrangement={arrangement}
                        />)
                    }
                    break;
                case "GameEnded":
                    return (<GameResult registerHovered={registerHovered}
                                        leaveGame={() => setGameOver(true)}
                                        renderHovered={renderHovered}/>)
                default:
                    break;
            }
            return <ChoosingDestination registerHovered={registerHovered} renderHovered={renderHovered}
                                        registerDrop={registerDrop} attributes={attributes}
                                        gameSocket={gameSocket} arrangement={arrangement} />
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
        <GameStateContext.Provider value={{
            gameState: gameState,
            setGameState: setState,
            registerHovered: registerHovered,
            renderHovered: renderHovered,
            registerDrop: registerDrop,
            haveBanished: haveBanished,
            haveSentToBottom: haveSentToBottom,
            remoteAttributes: remoteAttributes,
            sendMessage: sendMessage
        }}>
            <DndProvider backend={HTML5Backend}>
                {renderGameStage()}
            </DndProvider>
        </GameStateContext.Provider>
    )
}

export default Game;
