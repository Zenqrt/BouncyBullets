package dev.zenqrt.bouncybullets.game;

import dev.zenqrt.bouncybullets.game.event.impl.PaperEventNode;
import org.bukkit.event.Event;

public abstract class EventGameState extends GameState {

    protected final PaperEventNode<Event> eventNode = new PaperEventNode<>();

    public abstract void registerEvents();

    @Override
    protected void onStateStart() {
        registerEvents();
    }

    @Override
    protected void onStateEnd() {
        this.eventNode.unregisterAllListeners();
    }
}
