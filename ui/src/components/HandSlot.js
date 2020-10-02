import React, {useState, useEffect, useRef, createRef} from "react";
import cardImages from "../img/cards/cards";
import {HandCard} from "./HandCard";
import {DragPreviewImage, useDrag, useDrop} from 'react-dnd'
import {CardTypes, getDragType, getDropTypes} from "./CardTypes";
function HandSlot(props) {
    const [attributes, setAttributes] = useState([{}])
    const {attached, card} = props
    const [collectedProps, drop] = useDrop({
        accept: getDropTypes(card),
        drop: (c) => {
            props.registerDrop(c.index, props.index)
            console.warn(props.index + "got a drop of " + c.index)
        }
    })

    useEffect(() => {
        let refsRequired = 1
        if (attached) {
            refsRequired += attached.length
        }
//        setCardRefs(cardRefs => (
//                Array(refsRequired).fill().map((_, i) => cardRefs[i] || createRef())
//            ))
    }, [attached])

    let allCards = [props.card]
    if (props.attached) {
        allCards = [props.card, ...props.attached]
    }
    return (
        <div ref={drop}>
            {allCards.map((c, index) => (
                <HandCard key={c.index * 200}
                          index={c.index}
                          position={index}
                          name={c.data.name}
                          registerHovered={props.registerHovered}
                          cardType={getDragType(card)}
                          style={{
                              zIndex: index
                         }}
                />
            ))}
        </div>
    )
}

export default HandSlot;