import {useGameState} from "../context/GameState";
import React from "react";
import {Button, Options} from "../components/inputElements";
import {DndProvider} from "react-dnd";
import {HTML5Backend} from "react-dnd-html5-backend";
import Dungeon from "../components/Dungeon";
import Village from "../components/Village";
import PlayerHand from "../components/PlayerHand";
import AttributeValues from "../components/AttributeValues";
import {TargetIndexes} from "../components/SlotIndexes"
import {destroyForCards, getCardDestroysFromArrangement, getLowerMapFromArrangement} from "../services/Arrangement";
import buyImg from "../img/buy.png";
import {CardTypes} from "../components/CardTypes";

function Purchasing(props) {
    const {remoteAttributes, sendMessage, renderHovered} = useGameState()
    const endTurn = () => {
        sendMessage({
            messageType: "Purchase",
            data: {
                bought: props.arrangement[0].buying.map(c => c.data.name),
                destroyed: getLowerMapFromArrangement(props.arrangement, "destroyed"),
            }
        })
    }

    const doDestroy = () => {
        sendMessage({
            messageType: "Destroy",
            data: {
                cardNames: getCardDestroysFromArrangement(props.arrangement)
            }
        })
    }

    const renderChoices = () => {
        const bought = props.arrangement[0].buying ?? []
        if (bought.length === remoteAttributes['Buys']) {
            return (
                <Options key={5}>
                     ({destroyForCards(props.arrangement) && (<Button key={1} onClick={doDestroy}>Destroy</Button>)}
                    <Button key={2} onClick={endTurn}>Done</Button>
                </Options>
            )
        } else if (bought.length > remoteAttributes['Buys']) {
            return (
                <div key={5}>
                    <div key={6} style={{fontSize: "x-large"}}>You do not have that many buys</div>
                    <Options key={7}>
                        ({destroyForCards(props.arrangement) && (<Button onClick={doDestroy}>Destroy</Button>)}
                    </Options>
                </div>
            )
        } else {
            return (
                <div key={5}>
                    <div key={6} style={{fontSize: "x-large"}}>You have more Buys available</div>
                    <Options key={7}>
                        ({destroyForCards(props.arrangement) && (<Button onClick={doDestroy}>Destroy</Button>)}
                        <Button key={9} onClick={endTurn}>Skip Buys</Button>
                    </Options>
                </div>
            )
        }
    }

    const bought = props.arrangement[0].buying ?? []

    return (
        <DndProvider backend={HTML5Backend}>
            <div>
                <Dungeon key={1}
                         extraSlot={{
                             image: buyImg,
                             title: "Buy",
                             altText: "Buy",
                             dropTypes: CardTypes.VILLAGE,
                             cards: bought,
                             index: TargetIndexes.BuyIndex,
                             singleDrop: false
                        }}
                />
                <Village key={2} purchased={bought.map(c => c.data.name)} />
                <AttributeValues key={6} values={remoteAttributes} show={{
                    Gold: "Gold",
                    Buys: "Buys",
                    Experience: "Experience"
                }} />
                <PlayerHand key={4} arrangement={props.arrangement} />
                {renderChoices()}
                {renderHovered()}
            </div>
        </DndProvider>
    )
}

export default Purchasing;