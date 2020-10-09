import React from "react";
import styled from "styled-components";

import HandSlot from "./HandSlot";
import DestroySlot from "./DestroySlot";

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
                        <HandSlot cards={column[0]} index={index + ".1"} key={"1"} registerDrop={props.registerDrop}
                                  registerHovered={props.registerHovered} />
                        <DestroySlot cards={column[1]} index={column[1][0]} key={"2"} registerDrop={props.registerDrop}
                                     registerHovered={props.registerHovered} />
                    </Pair>)
                } else {
                    return (
                        <HandSlot cards={column[0]} index={index + ".1"} key={1} registerDrop={props.registerDrop}
                                  registerHovered={props.registerHovered} />
                    )
                }
            })}
        </HandContainer>
    )
}

export default PlayerHand;