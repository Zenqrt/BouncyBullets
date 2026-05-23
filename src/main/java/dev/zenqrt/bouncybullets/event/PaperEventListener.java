package dev.zenqrt.bouncybullets.event;

import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface PaperEventListener<T extends Event> {

    Class<T> getEventClass();
    EventPriority getPriority();
    void run(@NotNull T event);


    static <E extends Event> Builder<E> builder(Class<E> eventClass) {
        return new Builder<>(eventClass, EventPriority.NORMAL);
    }

    static <E extends Event> Builder<E> builder(Class<E> eventClass, EventPriority priority) {
        return new Builder<>(eventClass, priority);
    }

    class Builder<T extends Event> {

        private final Class<T> eventClass;
        private final List<Predicate<T>> filters = new ArrayList<>();
        private final EventPriority priority;
        private Consumer<T> handler;

        Builder(Class<T> eventClass, EventPriority priority) {
            this.eventClass = eventClass;
            this.priority = priority;
        }

        public Builder<T> filter(Predicate<T> filter) {
            this.filters.add(filter);
            return this;
        }

        public Builder<T> handler(Consumer<T> handler) {
            this.handler = handler;
            return this;
        }

        public PaperEventListener<T> build() {
            return new PaperEventListener<>() {
                @Override
                public Class<T> getEventClass() {
                    return eventClass;
                }

                @Override
                public EventPriority getPriority() {
                    return priority;
                }

                @Override
                public void run(@NotNull T event) {
                    if (!filters.isEmpty()) {
                        for (Predicate<T> filter : filters) {
                            if (!filter.test(event))
                                return;
                        }
                    }

                    handler.accept(event);
                }
            };
        }

    }
}
