import React from "react";
import {HandCard} from "./HandCard";
import {useDrop} from 'react-dnd'
import {CardTypes, getDropTypes} from "./CardTypes";
function DiscardSlot(props) {
    const [collectedProps, drop] = useDrop({
        accept: getDropTypes("All"),
        drop: (c) => {
            props.registerDrop(c.index, props.cards[0].index)
        }
    })

    return (
        <div ref={drop}>
            {props.cards.map((c, index) => (
                <HandCard key={103}
                          index={103}
                          name={"Discard"}
                          registerHovered={props.registerHovered}
                          registerDrop={props.registerDrop}
                          cardType={CardTypes.DISCARD}
                />
            ))}
        </div>
    )
}

export default DiscardSlot;