import React, {useState, useEffect} from "react";
import { useAuth } from "../context/auth";
import { GameStateContext } from "../context/GameState";
import {useParams} from "react-router";
import GameHeader from "../components/GameHeader"
import Startup from "./Startup";

function Game(props) {
    const [ gameState, setGameState ] = useState()
    const { authTokens } = useAuth();
    const [ error, setLastError ] = useState("")
    const [ reload, setReload] = useState(true)
    let game = useParams()
    let ws = null

    const setState = (data) => {
        setGameState(data);
    }

    useEffect(() => {
        ws = new WebSocket('ws://localhost:9000/api/game')
        ws.onopen = () => {
            ws.send('{"messageType": "Authentication", "data": {"token":"' +
                authTokens.token + '"}}')
            ws.send('{"messageType": "ConnectToGame", "data": {"gameId":' +
                game.gameId + '}}')
        }
        ws.onmessage = evt => {
            // listen to data sent from the websocket server
            const message = JSON.parse(evt.data)
            console.log(message)
        }
        ws.onclose = () => {
            console.log('disconnected')
            // automatically try to reconnect on connection loss
        }
    })

    const renderGameStage = () => {
        if (!gameState) {
            return ""
        }
        switch (gameState.currentStage.stage) {
            case "WaitingForPlayers":
                return <Startup reload={() => setReload(true)} />
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
