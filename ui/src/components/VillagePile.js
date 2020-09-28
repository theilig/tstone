import React, { useRef } from "react";
import cardImages from "../img/cards/cards"
import styled from "styled-components";
import {findDOMNode} from "react-dom";

const VillageCard = styled.img`
    width: 70px;
    height: 100px;
    margin-left: 10px;
`;

function VillagePile(props) {
    let card = props.pile.cards[0];
    const refContainer = useRef(null)
    const handleHovered = () => {
        if (refContainer && refContainer.current) {
            props.registerHovered(props.pile.cards[0], refContainer.current.getBoundingClientRect())
        }
    }
    if (card) {
        return (<VillageCard id={card.id} src={cardImages[card.name]} title={card.name}
                             ref={refContainer}
                             onMouseOver={() => handleHovered()} />)
    }
    return (
        <VillageCard id={0} src={cardImages['CardBack']} title={'CardBack'} />
    )
}

export default VillagePile;