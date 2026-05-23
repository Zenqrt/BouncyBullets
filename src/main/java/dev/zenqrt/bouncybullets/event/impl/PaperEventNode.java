package dev.zenqrt.bouncybullets.event.impl;

import dev.zenqrt.bouncybullets.BouncyBulletsPlugin;
import dev.zenqrt.bouncybullets.event.EventNode;
import dev.zenqrt.bouncybullets.event.PaperEventListener;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class PaperEventNode<E extends Event> implements EventNode<E> {

    private final List<Predicate<E>> nodeFilters = new ArrayList<>();
    private final List<ListenerInfo<? extends E>> listeners = new ArrayList<>();

    @SuppressWarnings("unchecked")
    @Override
    public <T extends E> void registerListener(PaperEventListener<T> eventListener) {
        ListenerInfo<? extends E> listenerInfo = new ListenerInfo<>(eventListener, new Listener() {});
        this.listeners.add(listenerInfo);

        Bukkit.getPluginManager().registerEvent(eventListener.getEventClass(), listenerInfo.bukkitListener, eventListener.getPriority(), (listener, event) -> {
            if (eventListener.getEventClass().isAssignableFrom(event.getClass())) {
                T eventCasted = (T) event;
                boolean passNodeFilters = this.nodeFilters.stream()
                                .allMatch(filter -> filter.test(eventCasted));

                if (!passNodeFilters)
                    return;

                eventListener.run(eventCasted);
            }
        }, BouncyBulletsPlugin.getInstance());
    }

    @Override
    public void unregisterAllListeners() {
        listeners.forEach(listener -> HandlerList.unregisterAll(listener.bukkitListener));
        listeners.clear();
    }

    @Override
    public void addNodeFilter(Predicate<E> filterCondition) {
        nodeFilters.add(filterCondition);
    }

    @Override
    public Collection<PaperEventListener<? extends E>> getListeners() {
        return listeners.stream()
                .map(ListenerInfo::listener)
                .collect(Collectors.toList());
    }

    private record ListenerInfo<E extends Event>(PaperEventListener<E> listener, Listener bukkitListener) {}
}
