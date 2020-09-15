import React, {useState, useEffect} from "react";
import { useAuth } from "../context/auth";
import { GameStateContext } from "../context/GameState";
import {useParams} from "react-router";
import Startup from "./Startup";
import {Redirect} from "react-router";

function Game(props) {
    const [ gameState, setGameState ] = useState()
    const [ gameSocket, setGameSocket ] = useState(null)
    const { authTokens } = useAuth()
    const [ gameOver, setGameOver] = useState(false)
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
            default:
                return ""
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
            {renderGameStage()}
        </GameStateContext.Provider>
    )
}

export default Game;
