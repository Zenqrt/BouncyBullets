package dev.zenqrt.bouncybullets.game.event;

import org.bukkit.event.Event;

import java.util.Collection;
import java.util.function.Predicate;

public interface EventNode<E extends Event> {

    <T extends E> void registerListener(PaperEventListener<T> listener);
    void unregisterAllListeners();

    void addGlobalFilter(Predicate<E> filterCondition);

    Collection<PaperEventListener<? extends E>> getListeners();
}
