import React from "react";
import styled from "styled-components";

import HandSlot from "./HandSlot";
import DestroySlot, {DESTROY_OFFSET} from "./DestroySlot";
import UpgradeSlot, {UPGRADE_OFFSET} from "./UpgradeSlot";
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
                if (column.length > 1) {
                    if (props.upgrading && props.upgrading.length > 0) {
                        return (<Pair key={index * SourceIndexes.HandOffset}>
                            <HandSlot
                                cards={column[0]}
                                index={column[0][0].data.sourceIndex}
                                key={column[0][0].data.sourceIndex}
                                registerDrop={props.registerDrop}
                                registerHovered={props.registerHovered}
                            />
                            <UpgradeSlot
                                cards={column[1]}
                                index={TargetIndexes.UpgradeIndex - TargetIndexes.HandIndex + column[0][0].data.sourceIndex}
                                key={TargetIndexes.UpgradeIndex - TargetIndexes.HandIndex + column[0][0].data.sourceIndex}
                                registerDrop={props.registerDrop}
                                registerUpgrade={props.registerUpgrade}
                                registerHovered={props.registerHovered}
                                name={column[0][0].data.name}
                                upgradee={column[0][0]}
                            />
                        </Pair>)
                    } else {
                        return (<Pair key={index * SourceIndexes.HandOffset}>
                            <HandSlot
                                cards={column[0]}
                                index={column[0][0].data.sourceIndex}
                                key={column[0][0].data.sourceIndex}
                                registerDrop={props.registerDrop}
                                registerHovered={props.registerHovered}
                            />
                            <DestroySlot
                                cards={column[1]}
                                index={TargetIndexes.DestroyIndex - TargetIndexes.HandIndex + column[0][0].data.sourceIndex}
                                key={TargetIndexes.DestroyIndex - TargetIndexes.HandIndex + column[0][0].data.sourceIndex}
                                registerDrop={props.registerDrop}
                                registerDestroy={props.registerDestroy}
                                registerHovered={props.registerHovered}
                                name={column[0][0].data.name}
                            />
                        </Pair>)

                    }
                } else {
                    return (
                        <HandSlot
                            cards={column[0]}
                            index={column[0][0].data.sourceIndex}
                            key={column[0][0].data.sourceIndex}
                            registerDrop={props.registerDrop}
                            registerHovered={props.registerHovered}
                        />
                    )
                }
            })}
        </HandContainer>
    )
}

export default PlayerHand;