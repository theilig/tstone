import HandCard from "./HandCard";
import cardImages from "../img/cards/cards";
import React from "react";

function WeaponCard(props) {
    return (<HandCard id={props.card.data.id} src={cardImages[props.card.data.name]} title={props.card.data.name} />)
}
export default WeaponCard;