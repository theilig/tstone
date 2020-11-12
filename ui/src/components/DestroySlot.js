import React from "react";
import { useDrop } from 'react-dnd'
import { getDropTypes } from "./CardTypes";
import trash from "../img/trash.png"
import { HandCard } from "./HandCard";
import {useGameState} from "../context/GameState";

function DestroySlot(props) {
    const {registerDrop} = useGameState()
    const [, drop] = useDrop({
        accept: getDropTypes({cardType: "TakeAny"}),
        drop: (c) => {
            registerDrop(c.card, props.index)
        }
    })

    return (
        <div ref={drop}>
            <img style={{width: '70px', height: '100px', marginLeft: '10px'}}
                 key={102} id={102}
                 src={trash} title={'Destroy'} alt={'Destroy'}
            />
            {props.cards.map((c, index) => (
                <HandCard key={c.data.sourceIndex}
                          card={c}
                          position={1}
                          style={{
                              zIndex: index,
                          }}
                />
            ))}
        </div>
    )
}

export default DestroySlot;