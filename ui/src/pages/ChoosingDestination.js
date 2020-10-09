import React from "react";
import { useAuth } from "../context/auth";
import { Button, Options } from "../components/inputElements"
import {useGameState} from "../context/GameState";
import Dungeon from "../components/Dungeon";
import Village from "../components/Village";
import PlayerHand from "../components/PlayerHand"
import AttributeValues from "../components/AttributeValues";

function ChoosingDestination(props) {
    const { gameState } = useGameState()
    const { authTokens } = useAuth()
    const renderChoices = () => {
        const stage = gameState.currentStage
        if (parseInt(authTokens.user.userId) === stage.data.currentPlayerId) {
            return (
                <Options>
                    <Button onClick={chooseVillage}>Go To Village</Button>
                    <Button>Go To Dungeon</Button>
                    <Button onClick={chooseRest}>Rest</Button>
                </Options>
            )
        }
    }

    const chooseRest = () => {
        props.gameSocket.send(JSON.stringify(
            {
                messageType: "ChooseRest",
                data: {
                    gameId: gameState.gameId
                }
            }
        ))
    }

    const chooseVillage = () => {
        props.gameSocket.send(JSON.stringify(
            {
                messageType: "ChooseVillage",
                data: {
                    gameId: gameState.gameId
                }
            }
        ))
    }

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
                }}
            />
            <PlayerHand registerHovered={props.registerHovered} arrangement={props.arrangement} />
            {renderChoices()}
            {props.renderHovered()}
        </div>
    )
}

export default ChoosingDestination;