import React, {useEffect, useState} from "react";
import { useAuth } from "../context/auth";
import axios from 'axios';
import {Redirect} from "react-router";
import GameListItem from "../components/GameListItem";
import styled from "styled-components";

const H2 = styled.h2`
    
`;
const Button = styled.button`
    background: linear-gradient(to bottom, #6371c7, #5563c1);
    border-color: #3f4eae;
    border-radius: 3px;
    padding: 1rem;
    color: white;
    font-weight: 700;
    width: 120px;
    margin-right: 10px;
    font-size: 0.8rem;
`;
const GameListBlock = styled.ul`
    list-style-type: none;
`;

const GameBlock = styled.li`
    border-style: double;
    max-width: 290px; 
`;

function GameList() {
    const { authTokens, setAuthTokens } = useAuth();
    const [ gameList, setGameList ] = useState({games:[]});
    const [ lastError, setLastError ] = useState( "" );
    const [ selectedGame, setSelectedGame ] = useState( 0 )
    const [ creatingGame, setCreatingGame ] = useState( false )
    function logOut() {
        setAuthTokens();
        setLastError("Logged out");
    }

    function createGame() {
        setCreatingGame(true)
    }

    useEffect(() => {
        const fetchData = async () => {
            await axios("/api/games", {
                method: "get",
                headers: {
                    'X-Requested-With': 'XMLHttpRequest',
                    'Authorization': 'Bearer ' + authTokens.token
                }
            }).then(result => {
                setGameList(result.data);
            }).catch(() => {
                setLastError("Could not retrieve games");
            });
        };
        fetchData()
        const tick = setInterval(fetchData, 60000)
        return (() => clearInterval(tick))
    }, [authTokens.token])

    if (lastError) {
        return (<Redirect to="/login" />);
    }

    if (creatingGame) {
        return (<Redirect push to="/createGame" />)
    }

    if (selectedGame > 0) {
        let location = "/game/" + selectedGame;
        return (<Redirect push to={location} />);
    }
    return (
        <div>
            <Button onClick={createGame}>New Game</Button>
            <Button onClick={logOut}> Log out</Button>
            <H2>Current Games</H2>
            <GameListBlock>
                {gameList && gameList.games.map((game) => (
                    <GameBlock key={game.gameId} onClick={() => {setSelectedGame(game.gameId)}}><GameListItem game={game} /></GameBlock>
                ))}
            </GameListBlock>
        </div>
    )
}

export default GameList;
