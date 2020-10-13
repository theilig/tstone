import React from "react";
import {useDrop} from 'react-dnd'
import {CardTypes, getDragType, getDropTypes} from "./CardTypes";
import battle from "../img/battle.png"
import {HandCard} from "./HandCard";
function BattleSlot(props) {
    const [, drop] = useDrop({
        accept: CardTypes.MONSTER,
        drop: (c) => {
            props.registerDrop(c.index, props.index)
        }
    })

    if (props.card) {
        return (
            <div ref={drop}>
                <img style={{width: '70px', height: '100px', marginLeft: '10px', marginRight: '20px'}}
                     key={102} id={102}
                     src={battle} title={'Battle'} alt={'Battle'}
                />
                <HandCard key={props.card.index * 200}
                          index={props.card.index}
                          small={true}
                          shiftHovered={true}
                          position={1}
                          name={props.card.data.name}
                          registerHovered={props.registerHovered}
                          registerDrop={props.registerDrop}
                          cardType={"Dungeon"}
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