package dev.zenqrt.bouncybullets.game.event.impl;

import dev.zenqrt.bouncybullets.game.event.EventListener;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface PaperEventListener<T extends Event> extends EventListener<T> {

    static <E extends Event> Builder<E> builder(Class<E> eventClass) {
        return new Builder<>(eventClass);
    }

    class Builder<T extends Event> {

        private final Class<T> eventClass;
        private final List<Predicate<T>> filters = new ArrayList<>();
        private Consumer<T> handler;

        public Builder(Class<T> eventClass) {
            this.eventClass = eventClass;
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
