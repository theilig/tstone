import React, { useState } from "react";
import { useAuth } from "../context/auth";
import styled from "styled-components";
import {Button, Error} from "../components/AuthForm"
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
    const MAX_PLAYERS = 5
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

    function acceptPlayer(userId) {
        props.gameSocket.send(
            JSON.stringify(
                {
                    messageType: "AcceptPlayer",
                    data: {
                        userId: userId
                    }
                }
            )
        )
    }

    function rejectPlayer(userId) {
        props.gameSocket.send(
            JSON.stringify(
                {
                    messageType: "RejectPlayer",
                    data: {
                        userId: userId
                    }
                }
            )
        )
    }

    const renderOptions = (isGameOwner) => {
        if (isGameOwner) {
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
        if (alreadyIn || playerCount >= MAX_PLAYERS) {
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

    const renderPlayer = (player, isGameOwner) => {
        if (player.pending && isGameOwner) {
            return (
                <div key={player.userId}>
                    <Options>
                        <div>{player.name} (Pending)</div>
                        <Button key={player.userId + "accept"} onClick={() => acceptPlayer(player.userId)}>accept</Button>
                        <Button key={player.userId + "reject"} onClick={() => rejectPlayer(player.userId)}>reject</Button>
                    </Options>
                </div>
            )
        }
        if (player.pending) {
            return (<div>{player.name} (Pending)</div>)
        }
        return (<div>{player.name}</div>)
    }

    let isGameOwner = false
    if (gameState.players[0] && authTokens.user.userId === gameState.players[0].userId) {
        isGameOwner = true
    }

    return (
        <div>
            <PlayerList>
                <div>Current Players</div>
                {gameState && gameState.players.map((player) => (
                        renderPlayer(player, isGameOwner)
                    )
                )}
            </PlayerList>
            {renderOptions(isGameOwner)}
            { lastError && <div><Error>{lastError}</Error></div> }
        </div>
    )
}

export default Startup;