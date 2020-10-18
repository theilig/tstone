import React from "react";
import {useDrop} from 'react-dnd'
import {CardTypes, getDragType, getDropTypes} from "./CardTypes";
import buy from "../img/buy.png"
import {HandCard} from "./HandCard";
function BuySlot(props) {
    const [, drop] = useDrop({
        accept: CardTypes.VILLAGE,
        drop: (c) => {
            props.registerDrop(c.card, props.index)
        }
    })

    return (
        <div ref={drop}>
            <img style={{width: '70px', height: '100px', marginLeft: '10px', marginRight: '20px'}}
                 key={102} id={102}
                 src={buy} title={'Purchase'} alt={'Purchase'}
            />
            {props.cards.map((c, index) => (
                <HandCard key={c.data.sourceIndex}
                          card={c}
                          small={true}
                          shiftHovered={true}
                          position={1}
                          registerHovered={props.registerHovered}
                          registerDrop={props.registerDrop}
                          style={{
                              zIndex: index + 1,
                          }}
                />
            ))}
        </div>
    )
}

export default BuySlot;