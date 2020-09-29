import {HandCard} from "./HandCard";
import cardImages from "../img/cards/cards";
import React, {useRef} from "react";

function VillagerCard(props) {
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
export default VillagerCard;
