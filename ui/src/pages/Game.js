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

function Game(props) {
    const [ gameState, setGameState ] = useState()
    const [ gameSocket, setGameSocket ] = useState(null)
    const { authTokens } = useAuth()
    const [ gameOver, setGameOver] = useState(false)
    const [ hovered, setHovered] = useState(null)

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
    }, [gameSocket, authTokens.token, gameId])

    const renderGameStage = () => {
        if (!gameState) {
            return ""
        }
        switch (gameState.currentStage.stage) {
            case "WaitingForPlayers":
                return <Startup gameSocket={gameSocket} />
            case "ChoosingDestination":
                return <ChoosingDestination registerHovered={registerHovered} renderHovered={renderHovered}
                                            gameSocket={gameSocket} />
            default:
                return <Resting registerHovered={registerHovered} renderHovered={renderHovered}
                                gameSocket={gameSocket} />
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
