import React, {useState} from "react";
import {useDrop} from 'react-dnd'
import {CardTypes, getDragType, getDropTypes} from "./CardTypes";
import upgrade from "../img/upgrade.png"
import {HandCard} from "./HandCard";

export const UPGRADE_OFFSET = 200

function UpgradeSlot(props) {
    const [, drop] = useDrop({
        accept: getDropTypes({cardType: "Upgrade"}),
        drop: (c) => {
            const newCard = props.registerUpgrade(props.upgradee, c.card.data.name)
            props.registerDrop(newCard, props.index)
        }
    })

    if (props.cards[0] != null) {
        return (
            <div ref={drop}>
                <img style={{width: '126px', height: '180px', marginLeft: '10px'}}
                     key={102} id={102}
                     src={upgrade} title={'Upgrade'} alt={'Upgrade'}
                />
                <HandCard position={1}
                          index={props.cards[0].sourceIndex}
                          card={props.cards[0]}
                          registerHovered={props.registerHovered}
                          registerDrop={props.registerDrop}
                          style={{
                              zIndex: 1,
                          }}
                />
            </div>
        )
    } else {
        return (
            <div ref={drop}>
                <img style={{width: '126px', height: '180px', marginLeft: '10px'}}
                     key={102} id={102}
                     src={upgrade} title={'Upgrade'} alt={'Upgrade'}
                />
            </div>
        )
    }
}

export default UpgradeSlot;