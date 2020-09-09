import React from "react";
import {useGameState} from "../context/GameState";
import {useAuth} from "../context/auth";
import styled from "styled-components";

const HeaderBlock = styled.div`
    display:flex;
    margin-left: 10px;
`;
const HeaderEntry = styled.div`
    margin-left: 10px;
`;
function GameHeader(props) {
    const { authTokens } = useAuth()
    const { gameState } = useGameState()
    if (gameState) {
        return (<HeaderBlock>
            </HeaderBlock>
        );
    } else {
        return null;
    }
}
export default GameHeader;
