import React, { useRef } from "react";
import cardImages from "../img/cards/cards"
import styled from "styled-components";
import { useDrag } from "react-dnd";
import { CardTypes } from "./CardTypes";

const VillageCard = styled.img`
    width: 70px;
    height: 100px;
    margin-left: 10px;
`;

export const removePurchased = (cards, purchased) => {
    let newCards = [...cards]
    let cardNames = newCards.map(c => c.name)
    purchased.forEach(name => {
        const index = cardNames.indexOf(name)
        if (index >= 0) {
            cardNames = cardNames.splice(index, 1)
            newCards = newCards.splice(index, 1)
        }
    })
    return newCards
}


function VillagePile(props) {
    const cards = removePurchased(props.pile, props.purchased)

    const getNames = () => {
        return cards.map(c => c.name).filter((value, index, self) => {
            return self.indexOf(value) === index
        })
    }
    const [,drag, preview] = useDrag({
        item: {
            type: CardTypes.VILLAGE,
            card: cards[0],
            names: getNames()
        },
    })

    let card = null
    if (cards.length > 0) {
        card = cards[0].data
    } else {
        card = {
            id: 1000 + props.id,
            name: "CardBack"
        }
    }
    const refContainer = useRef(null)
    const handleHovered = () => {
        if (refContainer && refContainer.current) {
            let name = null
            let foundUpgrade = false
            if (props.upgrading != null) {
                const names = cards.map(c => c.data.name).reverse()
                names.forEach(n => {
                    if (props.upgrading.includes(n)) {
                        foundUpgrade = true
                    } else if (!foundUpgrade) {
                        name = n
                    }
                })
            }
            if (name == null && cards[0] && cards[0].data) {
                name = cards[0].data.name
            }
            if (name) {
                props.registerHovered(name, refContainer.current.getBoundingClientRect())
            }
        }
    }
    if (card) {
        let style = {}
        if (props.upgrading && getNames().length === 1) {
            style =  {
                opacity: 0.4
            }
        }

        return (
            <div ref={drag}>
                <VillageCard
                    style={style}
                    data={card}
                    key={card.id} id={card.name} src={cardImages[card.name]} title={card.name}
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