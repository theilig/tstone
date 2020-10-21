import React from "react";
import styled from "styled-components";

import HandSlot from "./HandSlot";
import DestroySlot from "./DestroySlot";
import UpgradeSlot from "./UpgradeSlot";
import BanishSlot from "./BanishSlot"
import {SourceIndexes, TargetIndexes} from "./SlotIndexes";

const HandContainer = styled.div`
    display: flex;
    flex-direction: row;
    margin-bottom: 20px;
`;

const Pair = styled.div`
    display: flex;
    flex-direction: column;
`;

function PlayerHand(props) {
    return (
        <HandContainer>
            {props.arrangement.map((column, index) => {
                return (
                    <Pair key={index * SourceIndexes.HandOffset}>
                        <HandSlot
                            cards={column.cards}
                            index={column.cards[0].data.sourceIndex}
                            key={column.cards[0].data.sourceIndex}
                            registerDrop={props.registerDrop}
                            registerHovered={props.registerHovered}
                        />
                        {column.banish && (<BanishSlot
                                card={column.banish[0]}
                                index={TargetIndexes.BanishIndex - TargetIndexes.HandIndex + column.cards[0].data.sourceIndex}
                                key={TargetIndexes.BanishIndex - TargetIndexes.HandIndex + column.cards[0].data.sourceIndex}
                                registerDrop={props.registerDrop}
                                registerDestroy={props.registerDestroy}
                                registerHovered={props.registerHovered}
                                name={column.cards[0].data.name}
                            />)}
                        {column.upgrade && (
                            <UpgradeSlot
                                cards={column.upgrade}
                                index={TargetIndexes.UpgradeIndex - TargetIndexes.HandIndex + column.cards[0].data.sourceIndex}
                                key={TargetIndexes.UpgradeIndex - TargetIndexes.HandIndex + column.cards[0].data.sourceIndex}
                                registerDrop={props.registerDrop}
                                registerUpgrade={props.registerUpgrade}
                                registerHovered={props.registerHovered}
                                name={column.cards[0].data.name}
                                upgradee={column.cards[0]}
                            />
                        )}
                        {column.destroy && (
                            <DestroySlot
                                cards={column.destroy}
                                index={TargetIndexes.DestroyIndex - TargetIndexes.HandIndex + column.cards[0].data.sourceIndex}
                                key={TargetIndexes.DestroyIndex - TargetIndexes.HandIndex + column.cards[0].data.sourceIndex}
                                registerDrop={props.registerDrop}
                                registerDestroy={props.registerDestroy}
                                registerHovered={props.registerHovered}
                                name={column.cards[0].data.name}
                            />
                        )}
                    </Pair>
                )
            })}
        </HandContainer>
    )
}

export default PlayerHand;