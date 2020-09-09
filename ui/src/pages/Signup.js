import React, {useState} from "react";
import { Link } from "react-router-dom";
import axios from 'axios';
import logoImg from "../img/logo.png";
import { Card, Logo, Form, Input, Button, Error, Success, EmailRegex } from '../components/AuthForm';

function Signup() {
    const [email, setEmail] = useState("")
    const [password, setPassword] = useState("")
    const [confirmation, setConfirmation] = useState("")
    const [lastError, setLastError] = useState("");
    const [firstName, setFirstName] = useState("")
    const [lastName, setLastName] = useState("")
    const [success, setSuccess] = useState("")

    function postSignup() {
        if (validateForm()) {
            axios("api/signup", {
                data: {
                    email,
                    password,
                    firstName,
                    lastName
                },
                method: "post",
                headers: {'X-Requested-With': 'XMLHttpRequest'},
                withCredentials: true
            }).then(result => {
                setLastError("");
                setSuccess(result.data.message)
            }).catch(error => {
                if (error.response) {
                    setLastError(error.response.data)
                } else {
                    setLastError("There was an error trying to submit your request, please try again");
                }
            });
        }
    }
    function validateForm() {
        let validated = true;
        if (firstName.length === 0) {
            setLastError("you must enter a first name")
        }
        if (password !== confirmation) {
            setLastError("password does not match confirmation")
            validated = false;
        }
        if (email.length === 0) {
            setLastError("you need to provide an email");
            validated = false;
        }
        if (!EmailRegex.test(email)) {
            setLastError("Invalid email address");
            validated = false;
        }

        if (password.length < 8) {
            setLastError("You need to provide a password at least 8 characters")
            validated = false;
        }
        return validated;
    }

    return (
        <Card>
            <Logo src={logoImg}/>
            <Form>
                <Input
                    type="text"
                    value={firstName}
                    onChange={e => {
                        setFirstName(e.target.value);
                    }}
                    placeholder="First Name"
                />
                <Input
                    type="text"
                    value={lastName}
                    onChange={e => {
                        setLastName(e.target.value);
                    }}
                    placeholder="Last Initial/Name"
                />
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
                <Input
                    type="password"
                    value={confirmation}
                    onChange={e => {
                        setConfirmation(e.target.value);
                    }}
                    placeholder="confirm password"
                />
                <Button onClick={postSignup}>Sign Up</Button>
            </Form>
            <Link to="/login">Log into an existing account</Link>
            { lastError && <Error>{lastError}</Error> }
            { success && <Success>{success}</Success> }
        </Card>
    );
}

export default Signup;
