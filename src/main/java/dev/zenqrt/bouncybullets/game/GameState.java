package dev.zenqrt.bouncybullets.game;

import dev.zenqrt.bouncybullets.game.event.EventNode;

public abstract class GameState<T extends EventNode<?>> {

    protected final T eventNode;
    private boolean canMoveOn;

    public GameState(T eventNode) {
        this.eventNode = eventNode;
        this.canMoveOn = true;
    }

    public abstract void registerEvents();

    protected void onStateStart() {}
    protected void onStateEnd() {}

    public final void start() {
        registerEvents();
        onStateStart();
    }

    public final void end() {
        if (!canMoveOn)
            return;

        this.eventNode.unregisterAllListeners();
        onStateEnd();
    }

    public boolean canMoveOn() {
        return canMoveOn;
    }

    public void setCanMoveOn(boolean canMoveOn) {
        this.canMoveOn = canMoveOn;
    }
}
