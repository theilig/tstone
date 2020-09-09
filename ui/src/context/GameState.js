import { createContext, useContext} from "react";

export const GameStateContext = createContext();

export function useGameState() {
    return useContext(GameStateContext);
}
