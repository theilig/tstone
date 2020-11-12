import React from "react";
import {HandCard} from "./HandCard";
import {useDrop} from 'react-dnd'
import {getDropTypes} from "./CardTypes";
import discard from "../img/discard.png";
import {useGameState} from "../context/GameState";
function DiscardSlot(props) {
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
                 src={discard} title={'Loan'} alt={'Loan'}
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

export default DiscardSlot;