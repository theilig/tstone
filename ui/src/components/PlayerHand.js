import React, { useState } from "react";
import {useGameState} from "../context/GameState";
import { useAuth } from "../context/auth";
import cardImages from "../img/cards/cards"
import styled from "styled-components";

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
    let userPlayer = null
    gameState.players.map((player) => {
        if (authTokens.user.userId === player.userId) {
            userPlayer = player;
        }
    })
    if (userPlayer) {
        return (
            <HandContainer>
                {userPlayer.hand.map((card) => (
                    <HandCard id={card.data.id} src={cardImages[card.data.name]} title={card.data.name} />
                ))}
            </HandContainer>
        )
    } else {
        return <HandContainer />
    }
}

export default PlayerHand;