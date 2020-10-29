import {useGameState} from "../context/GameState";
import React from "react";
import {Button, Options} from "../components/inputElements";
import {DndProvider} from "react-dnd";
import {HTML5Backend} from "react-dnd-html5-backend";
import Dungeon from "../components/Dungeon";
import Village from "../components/Village";
import PlayerHand from "../components/PlayerHand";
import AttributeValues from "../components/AttributeValues";
import {getLowerMapFromArrangement} from "../services/Arrangement";
import { useAuth } from "../context/auth";

function Discarding(props) {
    const {gameState} = useGameState()
    const { authTokens } = useAuth()

    const endTurn = () => {
        const borrowsData = getLowerMapFromArrangement(props.arrangement, "loan")
        let borrows = []
        const borrowKeys = Object.keys(borrowsData)
        if (borrowKeys.length > 0) {
            borrows = borrowsData[borrowKeys[0]]
        }
        let discards = []
        const discardsData = getLowerMapFromArrangement(props.arrangement, "discard")
        const discardKeys = Object.keys(discardsData)
        if (discardKeys.length > 0) {
            discards = discardsData[discardKeys[0]]
        }
        props.gameSocket.send(JSON.stringify(
            {
                messageType: "Discard",
                data: {
                    gameId: gameState.gameId,
                    borrows: borrows,
                    discards: discards
                }
            }
        ))
    }

    const correctBorrows = () => {
        const discardData = getLowerMapFromArrangement(props.arrangement, "loan")
        let borrows = []
        if (discardData && discardData[0]) {
            borrows = discardData[0]
        }
        return borrows.length === gameState.currentStage.borrows ||
            props.arrangement.filter(a => a.cards[0].data.cardType === "HeroCard").length === 0
    }

    const correctDiscards = () => {
        const stage = gameState.currentStage
        let total = 0
        let heroDoubles = stage.data.heroesDouble
        props.arrangement.forEach(column => {
            if (column.discard != null) {
                column.discard.forEach(card => {
                    if (card.cardType === "HeroCard" && heroDoubles > 0) {
                        total += 2
                        heroDoubles -= 1
                    } else {
                        total += 1
                    }
                })
            }
        })
        return total === stage.data.howMany - stage.data.borrows
    }

    const renderChoices = () => {
        const stage = gameState.currentStage
        let playerDiscarding = stage.stage === "PlayerDiscard" && stage.data.howMany > stage.data.borrows &&
            stage.data.playerIds.includes(authTokens.user.userId)
        let playerLoaning = stage.stage === "PlayerDiscard" && stage.data.borrows > 0 &&
            stage.data.playerIds.includes(authTokens.user.userId)

        if (correctBorrows() && correctDiscards()) {
            return (
                <Options key={5}>
                    <Button key={3} onClick={endTurn}>Done</Button>
                </Options>
            )
        } else if (!playerDiscarding && !playerLoaning) {
            return (<div style={{fontSize: "x-large"}}>Waiting for discards</div>)
        } else {
            let instructions = ""
            if (stage.data.borrows === 1) {
                instructions = "You must discard 1 hero that can be borrowed."
            } else if (stage.data.borrows > 0) {
                instructions = "You must discard " + stage.data.borrows + " heroes that can be borrowed."
            }
            const discards = stage.data.howMany - stage.data.borrows
            if (discards > 0) {
                let discardText = discards + ' card.'
                if (discards > 1) {
                    discardText = discards + ' cards.'
                }
                instructions = instructions + " You must discard " + discardText
                if (stage.data.heroesDouble > 0) {
                    instructions = instructions + " up to " + stage.data.heroesDouble + " heroes will count as two cards"
                }
            }
            return (
                <div key={6} style={{fontSize: "x-large"}}>{instructions}</div>
            )
        }
    }

    const disabledStyle = {
        opacity: 0.4
    }

    return (
        <DndProvider backend={HTML5Backend}>
            <div>
                <div key={1} style={disabledStyle}>
                    <Dungeon registerHovered={props.registerHovered} />
                    <Village key={2} registerHovered={props.registerHovered} registerDrop={props.registerDrop}
                         purchased={[]} />
                </div>
                <AttributeValues key={3} values={props.attributes} show={{
                    goldValue: "Gold",
                    buys: "Buys",
                    experience: "Experience"
                }} />
                <PlayerHand key={4} registerHovered={props.registerHovered} registerDrop={props.registerDrop}
                            arrangement={props.arrangement} />
                {renderChoices()}
                {props.renderHovered()}
            </div>
        </DndProvider>
    )
}

export default Discarding;