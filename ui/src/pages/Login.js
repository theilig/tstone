import React, { useState } from "react";
import { Link, Redirect } from 'react-router-dom';
import axios from 'axios';
import logoImg from '../img/logo.jpg';
import { Card, Logo, Form, Input, Button, Error, EmailRegex } from '../components/AuthForm';
import { useAuth } from "../context/auth";

function Login(props) {
    const [isLoggedIn, setLoggedIn] = useState(false);
    const [lastError, setLastError] = useState("");
    const [email, setEmail] = useState("")
    const [password, setPassword] = useState("")
    const { setAuthTokens } = useAuth();
    const referrer = props.location.state ?
        (props.location.state.referrer.pathname || '/') :
        '/';

    function postLogin() {
        if (validateForm()) {
            axios("/api/login", {
                data: {
                    email,
                    password,
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
                } else {
                    // Something happened in setting up the request that triggered an Error
                    setLastError("Problem connecting to login server, please try again");
                }
            });
        }
    }

    function validateForm() {
        let validated = true;
        if (password.length < 0) {
            setLastError("You need to provide a password");
            validated = false;
        }
        if (email.length === 0 || !EmailRegex.test(email)) {
            setLastError("you need to sign in with an email");
            validated = false;
        }
        return validated;
    }

    if (isLoggedIn) {
        return <Redirect to={referrer} />;
    }

    return (
        <Card>
            <Logo src={logoImg} />
            <Form>
                <Input
                    type="email"
                    value={email}
                    onChange={e => {
                        setEmail(e.target.value);
                    }}
                    placeholder="email"
                />
                <Input
                    type="password"
                    value={password}
                    onChange={e => {
                        setPassword(e.target.value);
                    }}
                    placeholder="password"
                />
                <Button onClick={postLogin}>Sign In</Button>
            </Form>
            <Link to="/signup">Sign Up for New Account</Link>
            { lastError && <Error>{lastError}</Error> }
        </Card>
    );
}

export default Login;
