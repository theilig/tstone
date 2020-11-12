import React from "react";
import { HandCard } from "./HandCard";
import { useDrop } from 'react-dnd'
import { CardTypes, getDropTypes } from "./CardTypes";
import {useGameState} from "../context/GameState";
function HandSlot(props) {
    const {registerDrop} = useGameState()
    const [, drop] = useDrop({
        accept: [CardTypes.FOOD, CardTypes.WEAPON],
        canDrop: ((item) => {
            return getDropTypes(props.cards[0]).includes(item.type)
        }),
        drop: (c) => {
            registerDrop(c.card, props.cards[0].data.sourceIndex)
        }
    })

    return (
        <div ref={drop}>
            {props.cards.map((c, index) => (
                <HandCard key={c.data.sourceIndex}
                          card={c}
                          position={index}
                          style={{
                              zIndex: index
                         }}
                />
            ))}
        </div>
    )
}

export default HandSlot;