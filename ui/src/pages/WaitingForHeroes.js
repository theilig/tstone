import React from "react";
import Dungeon from "../components/Dungeon";
import Village from "../components/Village";
import PlayerHand from "../components/PlayerHand"
import AttributeValues from "../components/AttributeValues";

function WaitingForDiscards(props) {
    return (
        <div>
            <Dungeon registerHovered={props.registerHovered} />
            <Village registerHovered={props.registerHovered} />
            <AttributeValues values={props.attributes} show={{
                goldValue: "Gold",
                buys: "Buys",
                light: "Light",
                attack: "Attack",
                magicAttack: "Magic Attack",
                experience: "Experience"
                }}
            />
            <PlayerHand
                registerHovered={props.registerHovered}
                arrangement={props.arrangement}
                registerDrop={props.registerDrop}
            />
            <div key={6} style={{fontSize: "x-large"}}>Waiting For Discards</div>
            {props.renderHovered()}
        </div>
    )
}

export default WaitingForDiscards;