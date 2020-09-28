import {HandCard} from "./HandCard";
import cardImages from "../img/cards/cards";
import React from "react";

function VillagerCard(props) {
    const goldValue = () => {return props.card.data.goldValue}
    const light = () => {return 0}
    return (<HandCard id={props.card.data.id} src={cardImages[props.card.data.name]} title={props.card.data.name} />)
}
export default VillagerCard;
