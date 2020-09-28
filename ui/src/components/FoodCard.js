import React, { useState, useEffect } from "react";
import cardImages from "../img/cards/cards"
import {HandCard} from "./HandCard";
function FoodCard(props) {
    const goldValue = () => {return props.card.data.goldValue}
    const light = () => {return props.card.data.light}
    return (<HandCard id={props.card.data.id} src={cardImages[props.card.data.name]} title={props.card.data.name} />)
}
export default FoodCard;