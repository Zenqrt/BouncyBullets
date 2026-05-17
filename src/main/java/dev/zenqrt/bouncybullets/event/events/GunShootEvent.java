package dev.zenqrt.bouncybullets.event.events;

import dev.zenqrt.bouncybullets.item.items.guns.GunItem;
import dev.zenqrt.bouncybullets.loadout.gun.BulletProperties;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class GunShootEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final GunItem gunItem;
    private final Player shooter;
    private BulletProperties bulletProperties;

    public GunShootEvent(GunItem gunItem, Player shooter, BulletProperties bulletProperties) {
        this.gunItem = gunItem;
        this.shooter = shooter;
        this.bulletProperties = bulletProperties;
    }

    public GunItem getGunItem() {
        return gunItem;
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
