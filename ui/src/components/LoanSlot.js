import React from "react";
import { useDrop } from 'react-dnd'
import { CardTypes } from "./CardTypes";
import borrow from "../img/borrow.png"
import { HandCard } from "./HandCard";
import {useGameState} from "../context/GameState";
function LoanSlot(props) {
    const {registerDrop} = useGameState()
    const [, drop] = useDrop({
        accept: CardTypes.HERO,
        drop: (c) => {
            registerDrop(c.card, props.index)
        }
    })

    return (
        <div ref={drop}>
            <img style={{width: '70px', height: '100px', marginLeft: '10px', marginRight: '20px'}}
                 key={102} id={102}
                 src={borrow} title={'Loan'} alt={'Loan'}
            />
            {props.cards.map((c, index) => (
                <HandCard key={c.data.sourceIndex}
                          card={c}
                          small={true}
                          shiftHovered={true}
                          position={1}
                          style={{
                              zIndex: index + 1,
                          }}
                />
            ))}
        </div>
    )
}

export default LoanSlot;