import React from "react";
import { useAuth } from "../context/auth";
import { Button, Options } from "../components/inputElements"
import {useGameState} from "../context/GameState";
import Dungeon from "../components/Dungeon";
import Village from "../components/Village";
import PlayerHand from "../components/PlayerHand"
import AttributeValues from "../components/AttributeValues";

function WaitingForHeroes(props) {
    const { gameState } = useGameState()
    const { authTokens } = useAuth()

    return (
        <div>
            <Dungeon registerHovered={props.registerHovered} />
            <Village registerHovered={props.registerHovered} />
            <AttributeValues values={props.attributes} show={{
                goldValue: "Gold",
                buys: "Buys",
                light: "Light",
                attack: "Attack",
                magicAttack: "Magic Attack",
                experience: "Experience"
                }}
            />
            <PlayerHand
                registerHovered={props.registerHovered}
                arrangement={props.arrangement}
                registerDrop={props.registerDrop}
            />
            <div key={6} style={{fontSize: "x-large"}}>Waiting For Heroes</div>
            {props.renderHovered()}
        </div>
    )
}

export default WaitingForHeroes;