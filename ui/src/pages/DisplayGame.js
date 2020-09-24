import React, { useState } from "react";
import { useAuth } from "../context/auth";
import styled from "styled-components";
import {Button, Error} from "../components/AuthForm"
import {useGameState} from "../context/GameState";
import cardImages from "../img/cards/cards"
import Dungeon from "../components/Dungeon";
import Village from "../components/Village";
import PlayerHand from "../components/PlayerHand"

function DisplayGame(props) {
    return (
        <div>
            <Dungeon />
            <Village />
            <PlayerHand />
        </div>
    )
}

export default DisplayGame;