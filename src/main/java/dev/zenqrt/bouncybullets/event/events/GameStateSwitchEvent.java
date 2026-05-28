package dev.zenqrt.bouncybullets.event.events;

import dev.zenqrt.bouncybullets.game.base.GameState;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class GameStateSwitchEvent extends Event implements GameEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final GameState newState;
    private final BouncyBulletGame game;

    public GameStateSwitchEvent(BouncyBulletGame game, GameState newState) {
        this.game = game;
        this.newState = newState;
    }

    public GameState getNewState() {
        return newState;
    }

    @Override
    public BouncyBulletGame getGame() {
        return game;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
