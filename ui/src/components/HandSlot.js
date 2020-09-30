import React, {useState, useEffect, useRef, createRef} from "react";
import cardImages from "../img/cards/cards";
import {HandCard} from "./HandCard";
import {executeEffect, isEarlyEffect} from "../services/effects";
function HandSlot(props) {
    const [cardRefs, setCardRefs] = useState([]);
    const [attributes, setAttributes] = useState([{}])
    const {attached, card} = props
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
        <div>
            {allCards.map((c, index) => (
                <HandCard key={c.data.id + props.index * 200} id={c.data.id + props.index * 200}
                          src={cardImages[c.data.name]} title={c.data.name}
                          onMouseOver={() => handleHovered(index)}
                          ref={cardRefs[index]}
                          onMouseOut={() => props.registerHovered(null, null)}
                              style={{
                                  position: 'relative',
                                  top: index * 20 + 'px',
                                  left: 0,
                              }}
                />))
            }
        </div>
    )
}

export default HandSlot;