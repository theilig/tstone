import React from "react";
import {useDrop} from 'react-dnd'
import {getDropTypes} from "./CardTypes";
import {HandCard} from "./HandCard";
import {useGameState} from "../context/GameState";

function DungeonSlot(props) {
    const {registerDrop} = useGameState()
    const [, drop] = useDrop({
        accept: getDropTypes({cardType: "Dungeon"}),
        drop: (c) => {
            registerDrop(c.card, props.index)
        }
    })

    return (
        <div ref={drop}>
            <HandCard position={0}
                      index={props.cards[0].sourceIndex}
                      card={props.cards[0]}
                      style={{
                          zIndex: 1,
                      }}
                      small={true}
            />
        </div>
    )
}

export default DungeonSlot;