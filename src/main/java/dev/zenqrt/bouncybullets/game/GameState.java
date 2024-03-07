package dev.zenqrt.bouncybullets.game;

public abstract class GameState<E> {

    protected final GameEventHandler<E> eventHandler;

    public GameState(GameEventHandler<E> eventHandler) {
        this.eventHandler = eventHandler;
    }

    public abstract void registerEvents();

    protected final void start() {
        registerEvents();
    }

}
