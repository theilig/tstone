import React, {useState, useEffect, useRef} from "react";
import cardImages from "../img/cards/cards"
import {HandCard} from "./HandCard";
function FoodCard(props) {
    const refContainer = useRef(null)
    const handleHovered = () => {
        if (refContainer && refContainer.current) {
            props.registerHovered(props.card.data, refContainer.current.getBoundingClientRect())
        }
    }
    let id = props.card.data.id * 100 + props.index
    return (<HandCard id={id} src={cardImages[props.card.data.name]} title={props.card.data.name}
                      ref={refContainer}
                      onMouseOver={() => handleHovered()}
                      onMouseOut={() => props.registerHovered(null, null)} />)
}
export default FoodCard;