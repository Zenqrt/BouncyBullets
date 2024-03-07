package dev.zenqrt.bouncybullets.game.impl;

import dev.zenqrt.bouncybullets.game.GameState;

public class PaperGameState extends GameState<PaperGameEventHandler> {

    public PaperGameState(PaperGameEventHandler eventHandler) {
        super(eventHandler);
    }

    @Override
    public void registerEvents() {

    }
}
