import React from "react";
import {useDrop} from 'react-dnd'
import {CardTypes, getDragType, getDropTypes} from "./CardTypes";
import banish from "../img/banish.png"
import {HandCard} from "./HandCard";
function BanishSlot(props) {
    const [, drop] = useDrop({
        accept: CardTypes.MONSTER,
        drop: (c) => {
            props.registerDrop(c.card, props.index)
        }
    })

    if (props.card) {
        return (
            <div ref={drop}>
                <img style={{width: '70px', height: '100px', marginLeft: '10px', marginRight: '20px'}}
                     key={102} id={102}
                     src={banish} title={'Banish'} alt={'Banish'}
                />
                <HandCard key={props.card.data.sourceIndex}
                          card={props.card}
                          small={true}
                          shiftHovered={true}
                          position={1}
                          registerHovered={props.registerHovered}
                          registerDrop={props.registerDrop}
                          style={{
                              zIndex: 1,
                          }}
                />
            </div>
        )

    } else {
        return (
            <div ref={drop}>
                <img style={{width: '70px', height: '100px', marginLeft: '10px', marginRight: '20px'}}
                     key={102} id={102}
                    src={banish} title={'Banish'} alt={'Banish'}
                />
            </div>
        )
    }
}

export default BanishSlot;