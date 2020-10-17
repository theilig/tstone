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
            const newName = props.registerUpgrade(c.index, props.upgradee.data.name)
            props.registerDrop(newName, props.index)
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
                          index={props.index + 50}
                          name={props.cards[0].index}
                          registerHovered={props.registerHovered}
                          registerDrop={props.registerDrop}
                          cardType={CardTypes.HERO}
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