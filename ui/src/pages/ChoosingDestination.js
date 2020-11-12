import React from "react";
import { useAuth } from "../context/auth";
import { Button, Options } from "../components/inputElements"
import {useGameState} from "../context/GameState";
import Dungeon from "../components/Dungeon";
import Village from "../components/Village";
import PlayerHand from "../components/PlayerHand"
import AttributeValues from "../components/AttributeValues";

function ChoosingDestination(props) {
    const { gameState, remoteAttributes, sendMessage, renderHovered } = useGameState()
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
        sendMessage({
            messageType: messageType,
            data: {}
        })

    }

    return (
        <div>
            <Dungeon />
            <Village />
            <AttributeValues values={remoteAttributes} show={{
                Gold: "Gold",
                Buys: "Buys",
                Light: "Light",
                Attack: "Attack",
                "Magic Attack": "Magic Attack",
                Experience: "Experience"
            }} />
            <PlayerHand
                arrangement={props.arrangement}
            />
            {renderChoices()}
            {renderHovered()}
        </div>
    )
}

export default ChoosingDestination;