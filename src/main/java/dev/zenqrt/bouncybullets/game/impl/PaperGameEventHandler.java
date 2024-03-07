package dev.zenqrt.bouncybullets.game.impl;

import dev.zenqrt.bouncybullets.game.GameEventHandler;
import org.bukkit.event.Event;

import java.util.function.Consumer;

public final class PaperGameEventHandler implements GameEventHandler<Event> {

    @Override
    public <T extends Event> void registerEvent(Class<T> eventClass, Consumer<T> eventHandler) {

    }

    @Override
    public boolean unregisterEvent(Class<Event> eventClass) {
        return false;
    }

    @Override
    public boolean unregisterAllEvents() {
        return false;
    }
}
