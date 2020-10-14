import React, { useRef } from "react";
import cardImages from "../img/cards/cards"
import styled from "styled-components";
import {useDrag} from "react-dnd";
import {CardTypes} from "./CardTypes";

const VillageCard = styled.img`
    width: 70px;
    height: 100px;
    margin-left: 10px;
`;

function VillagePile(props) {
    const [,drag, preview] = useDrag({
        item: {type: CardTypes.VILLAGE, index: props.pile.cards[0].name},
        end: (item, monitor) => {
            if (!monitor.didDrop() && props.registerDrop) {
                props.registerDrop(props.index, null)
            }
        }
    })

    let card = null
    if (props.purchased.length < props.pile.cards.length) {
        card = props.pile.cards[props.purchased.length]
    } else {
        card = {
            id: 1000 + props.id,
            name: "CardBack"
        }
    }
    const refContainer = useRef(null)
    const handleHovered = () => {
        if (refContainer && refContainer.current) {
            props.registerHovered(props.pile.cards[0].name, refContainer.current.getBoundingClientRect())
        }
    }
    if (card) {
        return (
            <div ref={drag}>
                <VillageCard key={card.id} id={card.name} src={cardImages[card.name]} title={card.name}
                             ref={refContainer}
                             registerDrop={props.registerDrop}
                             onMouseOver={() => handleHovered(true)}
                             onMouseOut={() => props.registerHovered(null, null)}
                             onMouseDown={() => props.registerHovered(null, null)}
                />
            </div>
        )
    }
    return (
        <VillageCard key={0} id={0} src={cardImages['CardBack']} title={'CardBack'} />
    )
}

export default VillagePile;