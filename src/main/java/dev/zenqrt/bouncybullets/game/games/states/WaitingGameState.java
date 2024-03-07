package dev.zenqrt.bouncybullets.game.games.states;

import dev.zenqrt.bouncybullets.game.impl.PaperGameEventHandler;
import dev.zenqrt.bouncybullets.game.impl.PaperGameState;

public final class WaitingGameState extends PaperGameState {

    public WaitingGameState(PaperGameEventHandler eventHandler) {
        super(eventHandler);
    }

    @Override
    public void registerEvents() {

    }
}
