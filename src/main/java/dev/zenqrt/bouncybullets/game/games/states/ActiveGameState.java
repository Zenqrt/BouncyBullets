package dev.zenqrt.bouncybullets.game.games.states;

import dev.zenqrt.bouncybullets.game.impl.PaperGameEventHandler;
import dev.zenqrt.bouncybullets.game.impl.PaperGameState;

public final class ActiveGameState extends PaperGameState {

    public ActiveGameState(PaperGameEventHandler eventHandler) {
        super(eventHandler);
    }

    @Override
    public void registerEvents() {

    }
}
