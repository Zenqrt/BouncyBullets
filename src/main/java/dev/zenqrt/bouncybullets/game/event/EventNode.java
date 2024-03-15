package dev.zenqrt.bouncybullets.game.event;

import java.util.Collection;

public interface EventNode<T> {

    void registerListener(T listener);
    void unregisterAllListeners();

    Collection<T> getListeners();
}
