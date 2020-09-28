import React, { useState, useEffect } from "react";
import cardImages from "../img/cards/cards"
import {HandCard, DraggingCard} from "./HandCard";
import {useDrag} from "react-dnd";
import {CardTypes} from "./CardTypes";
function HeroCard(props) {
    const [{isDragging}, drag] = useDrag({
        item: {type: CardTypes.HERO},
        collect: monitor => ({
            isDragging: !!monitor.isDragging(),
        }),
    })
    let id = props.card.data.id * 100 + props.index
    return (<HandCard ref={drag} id={id} src={cardImages[props.card.data.name]} title={props.card.data.name} />)
}

export default HeroCard;