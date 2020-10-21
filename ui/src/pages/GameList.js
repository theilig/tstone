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
    function logOut() {
        setAuthTokens();
        setLastError("Logged out");
    }

    function createGame() {
        axios("/api/createGame", {
            method: "post",
            headers: {
                'X-Requested-With': 'XMLHttpRequest',
                'Authorization': 'Bearer ' + authTokens.token
            }
        }).then(result => {
            setSelectedGame(result.data.gameId)
        }).catch(error => {
            if (error.response) {
                setLastError(error.response.data)
            } else {
                // Something happened in setting up the request that triggered an Error
                setLastError("Problem creating the game, please try again");
            }
        });
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
        const tick = setInterval(fetchData, 15000)
        return (() => clearInterval(tick))
    }, [authTokens.token])

    if (lastError) {
        return (<Redirect to="/login" />);
    }

    if (selectedGame > 0) {
        let location = "/game/" + selectedGame;
        return (<Redirect push to={location} />);
    }
    if (!gameList || !gameList.games) {
        return (
            <div>
                <Button key={"1.1"} onClick={createGame}>New Game</Button>
                <Button key={"1.2"} onClick={logOut}> Log out</Button>
            </div>
        )
    }
    return (
        <div>
            <Button key={"1.1"} onClick={createGame}>New Game</Button>
            <Button key={"1.2"} onClick={logOut}> Log out</Button>
            <H2 key={"1.3"}>Current Games</H2>
            <GameListBlock key={"1.4"}>
                {gameList && gameList.games.map((game) => (
                    <GameBlock key={game.gameId} onClick={() => {setSelectedGame(game.gameId)}}>
                        <GameListItem key={game.gameId} game={game} />
                    </GameBlock>
                ))}
            </GameListBlock>
        </div>
    )
}

export default GameList;
