import React, { useState, useEffect } from "react";
import { Redirect } from 'react-router-dom';
import axios from 'axios';
import { useAuth } from "../context/auth";
import styled from "styled-components";
import {Button, Error} from "../components/AuthForm"

const GameCreation = styled.div`
    display: flex;
    margin-left: 10px;
`;

const PropertiesLabel = styled.div`
    line-height: 1.7;
`;

const PropertiesRow = styled.div`
    display: flex;
    margin-left: 10px;
    justify-content: space-between;
    width: 100%;
    align-content: center;

`;

const GameProperties = styled.div`
    display: flex;
    flex-direction: column;
    height: 100%;
    justify-content: space-between;
`;

function CreateGame(props) {
    const { authTokens } = useAuth();
    const [lastError, setLastError] = useState("");
    const [selectedGame, setSelectedGame] = useState(0)

    function createGame() {
        if (validateForm()) {
            axios("/api/createGame", {
                data: {
                },
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
    }

    function validateForm() {
        let validated = true;
        return validated;
    }

    if (selectedGame > 0) {
        let location = "/game/" + selectedGame;
        return (<Redirect to={location} />);
    }

    return (
        <div>
            <Button onClick={createGame}>Start Game</Button>
            {lastError && <Error>{lastError}</Error>}
        </div>
    );
}

export default CreateGame;
