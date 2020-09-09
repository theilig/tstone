import React, {useState, useEffect} from "react";
import axios from "axios";
import { useAuth } from "../context/auth";
import { GameStateContext } from "../context/GameState";
import {useParams} from "react-router";
import GameHeader from "../components/GameHeader"


function Game(props) {
    const [ gameState, setGameState ] = useState()
    const { authTokens } = useAuth();
    const [ error, setLastError ] = useState("")
    let game = useParams()

    const setState = (data) => {
        setGameState(data);
    }

    useEffect(() => {
        const fetchData = async () => {
            await axios("/api/game/state", {
                method: "get",
                params: {
                    gameId: game.gameId
                },
                headers: {
                    'X-Requested-With': 'XMLHttpRequest',
                    'Authorization': 'Bearer ' + authTokens.token
                }
            }).then(result => {
                setGameState(result.data);
            }).catch(() => {
                setLastError("Error Making Request");
            });
        };
        fetchData()
    }, [])

    const renderGameStage = () => {
        if (!gameState) {
            return ""
        }
        switch (gameState.stage) {
            case "PickDestination":
                return <div />
        }
    }

    return (
        <GameStateContext.Provider value={{ gameState, setGameState: setState  }}>
            <GameHeader />
            {renderGameStage()}
        </GameStateContext.Provider>
    )
}

export default Game;
