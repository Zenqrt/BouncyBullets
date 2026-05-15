package dev.zenqrt.bouncybullets.event.events;

import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerQuitGameEvent extends PlayerEvent implements GameEvent {

    private static final HandlerList HANDLERS = new HandlerList();
    private final BouncyBulletGame game;

    public PlayerQuitGameEvent(@NotNull Player who, BouncyBulletGame game) {
        super(who);

        this.game = game;
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
