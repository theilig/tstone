import React from "react";
import styled from "styled-components";

import HandSlot from "./HandSlot";
import DestroySlot, {DESTROY_OFFSET} from "./DestroySlot";

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
                    return (<Pair key={index}>
                        <HandSlot cards={column[0]} index={index + ".1"} key={index + ".1"} registerDrop={props.registerDrop}
                                  registerHovered={props.registerHovered} />
                        <DestroySlot cards={column[1]} index={DESTROY_OFFSET + index} key={index + ".2"}
                                     registerDrop={props.registerDrop} registerDestroy={props.registerDestroy}
                                     registerHovered={props.registerHovered} name={column[0][0].data.name}/>
                    </Pair>)
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