package dev.zenqrt.bouncybullets.event;

import dev.zenqrt.bouncybullets.event.impl.PaperEventNode;
import org.bukkit.event.Event;

import java.util.Collection;
import java.util.function.Predicate;

public interface EventNode<E extends Event> {

    static <T extends Event> EventNode<T> create() {
        return new PaperEventNode<>();
    }

    <T extends E> void registerListener(PaperEventListener<T> listener);
    void unregisterAllListeners();

    void addNodeFilter(Predicate<E> filterCondition);
    void addChild(EventNode<? extends E> childNode);

    Collection<EventNode<? extends E>> getChildren();
    Collection<PaperEventListener<? extends E>> getListeners();
}
