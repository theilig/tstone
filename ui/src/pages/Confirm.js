import React, { useState } from "react";
import { Redirect, useParams } from "react-router-dom";
import { useAuth } from "../context/auth";
import axios from 'axios';
import {Error} from "../components/AuthForm";

function Confirm() {
    const [isLoggedIn, setLoggedIn] = useState(false);
    const [lastError, setLastError] = useState("");
    const [invalidToken, setInvalidToken] = useState(false)
    const { setAuthTokens } = useAuth();
    let {token} = useParams();
    if (!invalidToken) {
        axios("/api/confirm", {
            data: {
                token
            },
            method: "post",
            headers: {'X-Requested-With': 'XMLHttpRequest'},
            withCredentials: true
        }).then(result => {
            setAuthTokens(result.data);
            setLoggedIn(true);

        }).catch(error => {
            if (error.response) {
                setLastError(error.response.data)
                setInvalidToken(true)
            } else {
                // Something happened in setting up the request that triggered an Error
                setLastError("Problem connecting to login server, please try again");
            }
        });
    }
    if (isLoggedIn) {
        return <Redirect to={"/"} />;
    }
    return (
        <Error>{lastError}</Error>
    );
}

export default Confirm;
