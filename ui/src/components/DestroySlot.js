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
            props.registerDestroy(c.data.name, props.name)
            props.registerDrop(c.data, props.index)
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
                          data={c.data}
                          position={1}
                          registerHovered={props.registerHovered}
                          registerDrop={props.registerDrop}
                          cardType={getDragType(c)}
                          style={{
                              zIndex: index,
                          }}
                />
            ))}
        </div>
    )
}

export default DestroySlot;