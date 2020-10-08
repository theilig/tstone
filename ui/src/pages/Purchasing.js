import {useGameState} from "../context/GameState";
import {useAuth} from "../context/auth";
import {useState} from "react";

function Purchasing(props) {
    const {gameState} = useGameState()
    const {authTokens} = useAuth()
    const [destroyed, setDestroyed] = useState(null)

    const endTurn = () => {
        props.gameSocket.send(JSON.stringify(
            {
                messageType: "Purchase",
                data: {
                    gameId: gameState.gameId,
                    cardName: destroyed
                }
            }
        ))
    }
}
export default Purchasing;