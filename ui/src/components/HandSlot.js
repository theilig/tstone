import React from "react";
import { HandCard } from "./HandCard";
import { useDrop } from 'react-dnd'
import { CardTypes, getDropTypes } from "./CardTypes";
function HandSlot(props) {
    const [, drop] = useDrop({
        accept: [CardTypes.FOOD, CardTypes.WEAPON],
        canDrop: ((item) => {
            return getDropTypes(props.cards[0]).includes(item.type)
        }),
        drop: (c) => {
            props.registerDrop(c.card, props.cards[0].data.sourceIndex)
        }
    })

    return (
        <div ref={drop}>
            {props.cards.map((c, index) => (
                <HandCard key={c.data.sourceIndex}
                          card={c}
                          position={index}
                          registerHovered={props.registerHovered}
                          registerDrop={props.registerDrop}
                          style={{
                              zIndex: index
                         }}
                />
            ))}
        </div>
    )
}

export default HandSlot;