package dev.zenqrt.bouncybullets.event;

import dev.zenqrt.bouncybullets.game.games.BulletProperties;
import dev.zenqrt.bouncybullets.game.games.Gun;
import dev.zenqrt.bouncybullets.game.games.GunProperties;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class GunShootEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Gun gun;
    private final Player shooter;
    private BulletProperties bulletProperties;

    public GunShootEvent(Gun gun, Player shooter, BulletProperties bulletProperties) {
        this.gun = gun;
        this.shooter = shooter;
        this.bulletProperties = bulletProperties;
    }

    public Gun getGun() {
        return gun;
    }

    public Player getShooter() {
        return shooter;
    }

    public BulletProperties getBulletProperties() {
        return bulletProperties;
    }

    public void setBulletProperties(BulletProperties bulletProperties) {
        this.bulletProperties = bulletProperties;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
