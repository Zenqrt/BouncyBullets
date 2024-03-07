package dev.zenqrt.bouncybullets.game;

import java.util.function.Consumer;

public interface GameEventHandler<E> {

    void registerEvent(Class<E> eventClass, Consumer<E> eventHandler);
    boolean unregisterEvent(Class<E> eventClass);
    boolean unregisterAllEvents();

}
