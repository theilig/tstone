import React from "react";
import {useDrop} from 'react-dnd'
import {getDropTypes} from "./CardTypes";
import upgrade from "../img/upgrade.png"
import {HandCard} from "./HandCard";
import {useGameState} from "../context/GameState";

function UpgradeSlot(props) {
    const {registerDrop} = useGameState()
    const [, drop] = useDrop({
        accept: getDropTypes({cardType: "Upgrade"}),
        drop: (c) => {
            const newCard = props.registerUpgrade(props.upgradee, c.card.data.name)
            registerDrop(newCard, props.index)
        }
    })

    if (props.cards[0] != null) {
        return (
            <div ref={drop}>
                <img style={{width: '70px', height: '100px', marginLeft: '10px'}}
                     key={102} id={102}
                     src={upgrade} title={'Upgrade'} alt={'Upgrade'}
                />
                <HandCard position={1}
                          index={props.cards[0].sourceIndex}
                          card={props.cards[0]}
                          style={{
                              zIndex: 1,
                          }}
                />
            </div>
        )
    } else {
        return (
            <div ref={drop}>
                <img style={{width: '70px', height: '100px', marginLeft: '10px'}}
                     key={102} id={102}
                     src={upgrade} title={'Upgrade'} alt={'Upgrade'}
                />
            </div>
        )
    }
}

export default UpgradeSlot;