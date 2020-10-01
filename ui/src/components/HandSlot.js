import React, {useState, useEffect, useRef, createRef} from "react";
import cardImages from "../img/cards/cards";
import {HandCard} from "./HandCard";
import {DragPreviewImage, useDrag, useDrop} from 'react-dnd'
import {CardTypes, getDragType, getDropTypes} from "./CardTypes";
function HandSlot(props) {
    const [cardRefs, setCardRefs] = useState([]);
    const [attributes, setAttributes] = useState([{}])
    const {attached, card} = props
    const [collectedProps, drop] = useDrop({
        accept: getDropTypes(card),
        drop: (c) => {
            console.warn("got a drop of " + c.index)
        }
    })

    const [{isDragging}, drag, preview] = useDrag({
        item: {type: getDragType(card), index: props.index},
        begin: (_) => console.warn("Drop begins")
    })


    useEffect(() => {
        let refsRequired = 1
        if (attached) {
            refsRequired += attached.length
        }
        setCardRefs(cardRefs => (
                Array(refsRequired).fill().map((_, i) => cardRefs[i] || createRef())
            ))
    }, [attached])

    const handleHovered = (index) => {
        if (cardRefs[index] && cardRefs[index].current) {
            let card = props.card
            if (index > 0) {
                card = props.attached[index - 1]
            }
            props.registerHovered(card.data, cardRefs[index].current.getBoundingClientRect())
        }
    }

    let allCards = [props.card]
    if (props.attached) {
        allCards = [props.card, ...props.attached]
    }
    return (
        <div ref={drop}>
            {allCards.map((c, index) => (
                <div key={props.index + 1} ref={drag}>
                    <HandCard key={c.data.id + props.index * 200} id={c.data.id + props.index * 200}
                              src={cardImages[c.data.name]} title={c.data.name}
                              onMouseOver={() => handleHovered(index)}
                              ref={cardRefs[index]}
                              onMouseDown={() => props.registerHovered(null, null)}
                              onMouseOut={() => props.registerHovered(null, null)}
                                  style={{
                                      position: 'relative',
                                      top: index * 20 + 'px',
                                      left: 0,
                                 }}
                    />
                </div>))
            }
        </div>
    )
}

export default HandSlot;