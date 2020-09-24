import React, { useState } from "react";
import cardImages from "../img/cards/cards"
import styled from "styled-components";
const VillageCard = styled.img`
    width: 70px;
    height: 100px;
    margin-left: 10px;
`;

function VillagePile(props) {
    let card = props.pile.cards[0];
    if (card) {
        return (<VillageCard id={card.id} src={cardImages[card.name]} title={card.name} />)
    }
    return (
        <VillageCard id={0} src={cardImages['CardBack']} title={'CardBack'} />
    )
}

export default VillagePile;