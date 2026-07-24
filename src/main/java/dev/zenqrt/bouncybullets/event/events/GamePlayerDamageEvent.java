package dev.zenqrt.bouncybullets.event.events;

import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class GamePlayerDamageEvent extends Event implements GamePlayerEvent, Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancelled;
    private final @Nullable BouncyBulletGamePlayer damager;
    private int damage;
    private final BouncyBulletGamePlayer gamePlayer;

    public GamePlayerDamageEvent(BouncyBulletGamePlayer gamePlayer, int damage, @Nullable BouncyBulletGamePlayer damager) {
        this.gamePlayer = gamePlayer;
        this.damage = damage;
        this.damager = damager;
    }

    @Override
    public BouncyBulletGamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public @Nullable BouncyBulletGamePlayer getDamager() {
        return damager;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getDamage() {
        return damage;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
