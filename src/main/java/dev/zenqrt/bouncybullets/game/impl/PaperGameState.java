package dev.zenqrt.bouncybullets.game.impl;

import dev.zenqrt.bouncybullets.game.GameState;
import dev.zenqrt.bouncybullets.game.event.impl.PaperEventNode;
import org.bukkit.event.Event;

public abstract class PaperGameState extends GameState<PaperEventNode<Event>> {

    public PaperGameState(PaperEventNode<Event> eventNode) {
        super(eventNode);
    }

    public PaperGameState() {
        this(new PaperEventNode<>());
    }
}
