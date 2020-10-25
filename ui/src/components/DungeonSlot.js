import React from "react";
import {useDrop} from 'react-dnd'
import {getDropTypes} from "./CardTypes";
import {HandCard} from "./HandCard";

function DungeonSlot(props) {
    const [, drop] = useDrop({
        accept: getDropTypes({cardType: "Dungeon"}),
        drop: (c) => {
            props.registerDrop(c.card, props.index)
        }
    })

    return (
        <div ref={drop}>
            <HandCard position={0}
                      index={props.cards[0].sourceIndex}
                      card={props.cards[0]}
                      registerHovered={props.registerHovered}
                      registerDrop={props.registerDrop}
                      style={{
                          zIndex: 1,
                      }}
                      small={true}
            />
        </div>
    )
}

export default DungeonSlot;