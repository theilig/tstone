import React from "react";
import styled from "styled-components";

const Summary = styled.div`
    box-sizing: border-box;
    max-width: 200px;
    max-height: 100px;
    padding: 0 2rem;
    display: flex;
    align-items: left;
`;

const Team = styled.div`
    display: flex;
    width: 100%;
    max-height: 50px;
    font-weight: 400;
    font-size: large;
    min-width: 175px;
`;

function GameListItem(props) {
    return (
        <Summary>
            <Team>Started by {props.game.state.players[0].name}</Team>
            {props.game.state.players.slice(1).map((player, index) => (
                <Team key={index}>{player.name}</Team>
            ))}
        </Summary>
    );
}

export default GameListItem;
