import React from "react";
import styled from "styled-components";

import HandSlot from "./HandSlot";
import DestroySlot, {DESTROY_OFFSET} from "./DestroySlot";
import UpgradeSlot, {UPGRADE_OFFSET} from "./UpgradeSlot";

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
                    if (props.upgrading.length > 0) {
                        return (<Pair key={index}>
                            <HandSlot cards={column[0]} index={index + ".1"} key={index + ".1"} registerDrop={props.registerDrop}
                                      registerHovered={props.registerHovered} />
                            <UpgradeSlot
                                cards={column[1]} index={UPGRADE_OFFSET + index} key={index + ".2"}
                                registerDrop={props.registerDrop} registerUpgrade={props.registerUpgrade}
                                registerHovered={props.registerHovered} name={column[0][0].data.name}
                                upgradee={column[0][0]}
                            />
                        </Pair>)
                    } else {
                        return (<Pair key={index}>
                            <HandSlot cards={column[0]} index={index + ".1"} key={index + ".1"} registerDrop={props.registerDrop}
                                      registerHovered={props.registerHovered} />
                            <DestroySlot cards={column[1]} index={DESTROY_OFFSET + index} key={index + ".2"}
                                         registerDrop={props.registerDrop} registerDestroy={props.registerDestroy}
                                         registerHovered={props.registerHovered} name={column[0][0].data.name}/>
                        </Pair>)

                    }
                } else {
                    return (
                        <HandSlot cards={column[0]} index={index + ".1"} key={index + ".1"} registerDrop={props.registerDrop}
                                  registerHovered={props.registerHovered} />
                    )
                }
            })}
        </HandContainer>
    )
}

export default PlayerHand;