package dev.zenqrt.bouncybullets.game;

import java.util.function.Consumer;

public interface GameEventHandler<E> {

    <T extends E> void registerEvent(Class<T> eventClass, Consumer<T> eventHandler);
    boolean unregisterEvent(Class<E> eventClass);
    boolean unregisterAllEvents();

}
