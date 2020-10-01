import React, { useRef } from "react";
import cardImages from "../img/cards/cards"
import styled from "styled-components";

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
            props.registerHovered(props.pile.cards[0].name, refContainer.current.getBoundingClientRect())
        }
    }
    if (card) {
        return (<VillageCard key={card.id} id={card.id} src={cardImages[card.name]} title={card.name}
                             ref={refContainer}
                             onMouseOver={() => handleHovered()}
                             onMouseOut={() => props.registerHovered(null, null)}/>)
    }
    return (
        <VillageCard key={0} id={0} src={cardImages['CardBack']} title={'CardBack'} />
    )
}

export default VillagePile;