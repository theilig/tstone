import React, {useEffect, useState} from "react";
import {HandCard} from "./HandCard";
import {useDrop} from 'react-dnd'
import {CardTypes, getDragType, getDropTypes} from "./CardTypes";
function HandSlot(props) {
    const [dropTypes, setDropTypes] = useState([])
    const [collectedProps, drop] = useDrop({
        accept: [CardTypes.FOOD, CardTypes.WEAPON],
        canDrop: ((item) => {
            return getDropTypes(props.cards[0]).includes(item.type)
        }),
        drop: (c) => {
            props.registerDrop(c.index, props.cards[0].index)
        }
    })

    useEffect(() => {
        setDropTypes(getDropTypes(props.cards[0]))
    }, [props.cards])

    return (
        <div ref={drop}>
            {props.cards.map((c, index) => (
                <HandCard key={c.index * 200}
                          index={c.index}
                          position={index}
                          name={c.data.name}
                          registerHovered={props.registerHovered}
                          registerDrop={props.registerDrop}
                          cardType={getDragType(c)}
                          style={{
                              zIndex: index
                         }}
                />
            ))}
        </div>
    )
}

export default HandSlot;