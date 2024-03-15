package dev.zenqrt.bouncybullets.game;

import dev.zenqrt.bouncybullets.game.event.EventNode;

public abstract class GameState<T extends EventNode<?>> {

    protected final T eventNode;

    public GameState(T eventNode) {
        this.eventNode = eventNode;
    }

    public abstract void registerEvents();

    protected void onStateStart() {}
    protected void onStateEnd() {}

    protected final void start() {
        onStateStart();
    }

    protected final void end() {
        onStateEnd();
    }

}
