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
                    <Button onClick={() => chooseDestination("ChooseVillage")}>Go To Village</Button>
                    <Button onClick={() => chooseDestination("ChooseDungeon")}>Go To Dungeon</Button>
                    <Button onClick={() => chooseDestination("ChooseRest")}>Rest</Button>
                </Options>
            )
        }
    }

    const chooseDestination = (messageType) => {
        props.gameSocket.send(JSON.stringify(
            {
                messageType: messageType,
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
            <PlayerHand
                registerHovered={props.registerHovered}
                arrangement={props.arrangement}
                registerDrop={props.registerDrop}
            />
            {renderChoices()}
            {props.renderHovered()}
        </div>
    )
}

export default ChoosingDestination;