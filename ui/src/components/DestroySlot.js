import React from "react";
import {useDrop} from 'react-dnd'
import {getDragType, getDropTypes} from "./CardTypes";
import trash from "../img/trash.png"
import {HandCard} from "./HandCard";

export const DESTROY_OFFSET = 100

function DestroySlot(props) {

    const [, drop] = useDrop({
        accept: getDropTypes({cardType: "TakeAny"}),
        drop: (c) => {
            props.registerDrop(c.card, props.index)
        }
    })

    return (
        <div ref={drop}>
            <img style={{width: '126px', height: '180px', marginLeft: '10px'}}
                 key={102} id={102}
                 src={trash} title={'Destroy'} alt={'Destroy'}
            />
            {props.cards.map((c, index) => (
                <HandCard key={c.data.sourceIndex}
                          card={c}
                          position={1}
                          registerHovered={props.registerHovered}
                          registerDrop={props.registerDrop}
                          style={{
                              zIndex: index,
                          }}
                />
            ))}
        </div>
    )
}

export default DestroySlot;