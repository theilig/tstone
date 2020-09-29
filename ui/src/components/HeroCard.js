import React, {useState, useEffect, useRef} from "react";
import cardImages from "../img/cards/cards"
import {HandCard, DraggingCard} from "./HandCard";
import {useDrag, useDrop} from "react-dnd";
import {CardTypes} from "./CardTypes";
function HeroCard(props) {
    const refContainer = useRef(null)
    const handleHovered = () => {
        if (refContainer && refContainer.current) {
            props.registerHovered(props.card.data, refContainer.current.getBoundingClientRect())
        }
    }
    const [{isDragging}, drag] = useDrag({
        item: {type: CardTypes.HERO},
        collect: monitor => ({
            isDragging: !!monitor.isDragging(),
        }),
    })
    let id = props.card.data.id * 100 + props.index
    return (
        <div ref={refContainer}
             onMouseOver={() => handleHovered()}
             onMouseOut={() => props.registerHovered(null, null)}
             onMouseDown={() => props.registerHovered(null, null)}>
            <HandCard key={id} id={id} src={cardImages[props.card.data.name]} title={props.card.data.name}
                      ref={drag} />
        </div>
    )
}

export default HeroCard;