import React from "react";
import { useDrop } from 'react-dnd'
import { CardTypes } from "./CardTypes";
import battle from "../img/battle.png"
import { HandCard } from "./HandCard";
import {useGameState} from "../context/GameState";
function BattleSlot(props) {
    const {registerDrop} = useGameState()
    const [, drop] = useDrop({
        accept: CardTypes.MONSTER,
        drop: (c) => {
            registerDrop(c.card, props.index)
        }
    })

    if (props.card) {
        return (
            <div ref={drop}>
                <img style={{width: '70px', height: '100px', marginLeft: '10px', marginRight: '20px'}}
                     key={102} id={102}
                     src={battle} title={'Battle'} alt={'Battle'}
                />
                <HandCard key={props.card.data.sourceIndex}
                          card={props.card}
                          small={true}
                          shiftHovered={true}
                          position={1}
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
                    src={battle} title={'Battle'} alt={'Battle'}
                />
            </div>
        )
    }
}

export default BattleSlot;