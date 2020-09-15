import React, { useState } from "react";
import axios from 'axios';
import { useAuth } from "../context/auth";
import styled from "styled-components";
import {Button, Error} from "../components/AuthForm"
import { Redirect } from 'react-router-dom';
import {useGameState} from "../context/GameState";

const PlayerList = styled.div`
    display: flex;
    flex-direction: column;
    margin-left: 10px;
    font-size: xx-large;
`;

const Options = styled.div`
    display: flex;
    flex-direction: row;
    max-width:410px;
`;

function Startup(props) {
    const { authTokens } = useAuth()
    const { gameState } = useGameState()
    const [ lastError, setLastError ] = useState("")
    const [ cancelled, setCancelled ] = useState(false)
    const MAX_PLAYERS = 4
    function leaveGame() {
        props.gameSocket.send(
            JSON.stringify(
                {
                    messageType: "LeaveGame"
                }
            )
        )
    }
    function requestToJoin() {
        props.gameSocket.send(
            JSON.stringify(
                {
                    messageType: "JoinGame"
                }
            )
        )
    }
    function startGame() {
        props.gameSocket.send(
            JSON.stringify(
                {
                    messageType: "StartGame"
                }
            )
        )
    }
    if (cancelled) {
        return (<Redirect to={"/"} />)
    }

    const renderOptions = () => {
        if (authTokens.user.userId === gameState.ownerId) {
            return (
                <Options>
                    <Button onClick={startGame}>Start Game</Button>
                    <Button onClick={leaveGame}>Leave Game</Button>
                </Options>
            )
        }
        let alreadyIn = false
        let playerCount = 0
        for (let i=0; i<gameState.players.length; i++) {
            if (gameState.players[i].userId === authTokens.user.userId) {
                alreadyIn = true;
            }
            if (!gameState.players[i].pending) {
                playerCount += 1
            }
        }
        if (alreadyIn) {
            return (
                <Options>
                    <Button onClick={leaveGame}>Leave Game</Button>
                </Options>
            )
        }
        if (playerCount >= MAX_PLAYERS) {
            return (
                <Options>
                    <Button onClick={leaveGame}>Leave Game</Button>
                </Options>
            )
        }
        return (
            <Options>
                <Button onClick={requestToJoin}>Ask to Join</Button>
                <Button onClick={leaveGame}>Leave Game</Button>
            </Options>
        )
    }

    return (
        <div>
            <PlayerList>
                <div>Current Players</div>
                {gameState && gameState.players.map((player) => (
                    <div>{player.name}</div>
                ))}
            </PlayerList>
            {renderOptions()}
            { lastError && <div><Error>{lastError}</Error></div> }
        </div>
    )
}

export default Startup;