import HandCard from "./HandCard";
import cardImages from "../img/cards/cards";
import React from "react";

function MonsterCard(props) {
    const goldValue = () => {return props.card.data.goldValue}
    const light = () => {return props.card.data.light}
    return (<HandCard id={props.card.data.id} src={cardImages[props.card.data.name]} title={props.card.data.name} />)
}

export default MonsterCard;