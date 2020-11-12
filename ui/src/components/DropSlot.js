import React from "react";
import { useDrop } from 'react-dnd'
import { HandCard } from "./HandCard";
import {useGameState} from "../context/GameState";
function DropSlot(props) {
    const {registerDrop} = useGameState()
    const [, drop] = useDrop({
        accept: props.slotInfo.dropTypes,
        drop: (c) => {
            registerDrop(c.card, props.slotInfo.index, props.slotInfo.singleDrop)
        }
    })

    return (
        <div ref={drop} style={{display: 'flex', marginRight: '20px'}}>
            <img style={{width: '70px', height: '100px', marginLeft: '10px', marginRight: '20px'}}
                 key={102} id={102}
                 src={props.slotInfo.image} title={props.slotInfo.title} alt={props.slotInfo.altText}
            />
            {props.slotInfo.cards.map((c, index) => (
                <HandCard key={c.data.sourceIndex}
                          card={c}
                          small={true}
                          shiftHovered={false}
                          position={index + 1}
                          rightShift={true}
                          style={{
                              zIndex: index + 1,
                          }}
                />
            ))}
        </div>
    )
}

export default DropSlot;