package dev.zenqrt.bouncybullets.game.event;

import org.jetbrains.annotations.NotNull;

public interface EventListener<T> {

    Class<T> getEventClass();
    void run(@NotNull T event);
}
