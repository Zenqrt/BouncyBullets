package dev.zenqrt.bouncybullets.game;

public abstract class GameState<H extends GameEventHandler<?>> {

    protected final H eventHandler;

    public GameState(H eventHandler) {
        this.eventHandler = eventHandler;
    }

    public abstract void registerEvents();

    protected void onStateStart() {}
    protected void onStateEnd() {}

    protected final void start() {
        registerEvents();
        onStateStart();
    }

    protected final void end() {
        eventHandler.unregisterAllEvents();
        onStateEnd();
    }

}
