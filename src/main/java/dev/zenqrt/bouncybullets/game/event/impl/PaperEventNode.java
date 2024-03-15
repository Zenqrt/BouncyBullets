package dev.zenqrt.bouncybullets.game.event.impl;

import dev.zenqrt.bouncybullets.BouncyBullets;
import dev.zenqrt.bouncybullets.game.event.EventNode;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("rawtypes")
public final class PaperEventNode<E extends Event> implements EventNode<PaperEventListener> {

    private final List<ListenerInfo<? extends E>> listeners = new ArrayList<>();

    @SuppressWarnings("unchecked")
    @Override
    public void registerListener(PaperEventListener eventListener) {
        ListenerInfo<? extends E> listenerInfo = new ListenerInfo<>(eventListener, new Listener() {});
        this.listeners.add(listenerInfo);

        Bukkit.getPluginManager().registerEvent(eventListener.getEventClass(), listenerInfo.bukkitListener, EventPriority.NORMAL, (listener, event) -> {
            if (eventListener.getEventClass().isAssignableFrom(event.getClass())) {
                eventListener.run(event);
            }
        }, BouncyBullets.getInstance());
    }

    @Override
    public void unregisterAllListeners() {
        listeners.forEach(listener -> HandlerList.unregisterAll(listener.bukkitListener));
        listeners.clear();
    }

    @Override
    public Collection<PaperEventListener> getListeners() {
        return listeners.stream()
                .map(ListenerInfo::listener)
                .collect(Collectors.toList());
    }

    private record ListenerInfo<E extends Event>(PaperEventListener<E> listener, Listener bukkitListener) {}
}
