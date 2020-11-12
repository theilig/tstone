import React from "react";
import {HandCard} from "./HandCard";
function PlayerResult(props) {
    const vpCards = props.cards.filter(c => c.data.victoryPoints != null && c.data.victoryPoints > 0)
    const pointTotal = vpCards.reduce((total, card) => {
        return (total ?? 0) + card.data.victoryPoints
    }, 0)
    return (
        <div>
            <div>{props.name} {pointTotal} Points </div>
            {vpCards.map((c, index) => (
                <HandCard key={c.data.sourceIndex}
                          card={c}
                          position={index}
                          style={{
                              zIndex: 100 - index
                         }}
                />
            ))}
        </div>
    )
}

export default PlayerResult;