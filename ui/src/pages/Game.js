import React, {useState, useEffect} from "react";
import { useAuth } from "../context/auth";
import { GameStateContext } from "../context/GameState";
import {useParams} from "react-router";
import GameHeader from "../components/GameHeader"
import Startup from "./Startup";

function Game(props) {
    const [ gameState, setGameState ] = useState()
    const [ gameSocket, setGameSocket ] = useState(null)
    const { authTokens } = useAuth()
    const [ error, setLastError ] = useState("")
    const [ reload, setReload] = useState(true)
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
                }
                console.log(message)
            }
            ws.onclose = () => {
                console.log('disconnected')
                // automatically try to reconnect on connection loss
            }
        }
    })

    const renderGameStage = () => {
        if (!gameState) {
            return ""
        }
        switch (gameState.currentStage.stage) {
            case "WaitingForPlayers":
                return <Startup gameSocket={gameSocket} />
        }
        return ""
    }

    return (
        <GameStateContext.Provider value={{ gameState, setGameState: setState  }}>
            <GameHeader />
            {renderGameStage()}
        </GameStateContext.Provider>
    )
}

export default Game;
