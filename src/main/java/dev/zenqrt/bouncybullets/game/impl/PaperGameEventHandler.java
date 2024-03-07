package dev.zenqrt.bouncybullets.game.impl;

import dev.zenqrt.bouncybullets.BouncyBullets;
import dev.zenqrt.bouncybullets.game.GameEventHandler;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.function.Consumer;

public final class PaperGameEventHandler implements GameEventHandler<Event> {

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Event> void registerEvent(Class<T> eventClass, Consumer<T> eventHandler) {
        Bukkit.getPluginManager().registerEvent(
                eventClass,
                new Listener() {}, EventPriority.NORMAL,
                (listener, event) -> eventHandler.accept((T) event),
                BouncyBullets.getInstance());
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
