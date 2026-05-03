package dev.zenqrt.bouncybullets.game.games;

import dev.zenqrt.bouncybullets.item.items.*;
import org.bukkit.inventory.ItemStack;

import java.util.function.Function;

public enum Gun {
    PISTOL(new GunProperties(5, 0.05, 0.02, 8, 5, 2), new BulletProperties(3, 200, -0.10F, 1, 5, 0.1F, 25, 5), PistolGunItem::new),
    SILENCED_PISTOL(new GunProperties(5, 0.05, 0.02, 8, 5, 2), new BulletProperties(3, 200, -0.10F, 1, 5, 0.1F, 25, 5), SilencedPistolGunItem::new),
    SMG(new GunProperties(1, 0.05, 0.02, 32, 2, 2), new BulletProperties(2, 350, -0.2F, 0.25, 1, 0.2F, 15, 2.5), SMGGunItem::new),
    SNIPER_RIFLE(new GunProperties(20, 0.1, 0.005, 3, 20, 5), new BulletProperties(2, 1000, -0.5F, 8, 8, -0.5F, 100, 0), SniperRifleGunItem::new),
    GRENADE_LAUNCHER(new GunProperties(40, 0.1, 0.02, 6, 20, 2), new BulletProperties(2, 1.25, 0, 10, 10, 0, 100, 0), GrenadeLauncherGunItem::new),
    DESERT_EAGLE(new GunProperties(10, 0.05, 0.02, 6, 10, 2), new BulletProperties(3, 500, -0.1F, 1, 7, 0.1F, 25, 4), DesertEagleGunItem::new);

    private final GunProperties gunProperties;
    private final BulletProperties bulletProperties;
    private final GunItem item;

    Gun(GunProperties gunProperties, BulletProperties bulletProperties, Function<Gun, GunItem> itemFunction) {
        this.gunProperties = gunProperties;
        this.bulletProperties = bulletProperties;
        this.item = itemFunction.apply(this);
    }

    public GunProperties getGunProperties() {
        return gunProperties;
    }

    public BulletProperties getBulletProperties() {
        return bulletProperties;
    }

    public GunItem getItem() {
        return item;
    }

    public ItemStack buildItemStack() {
        return item.buildItemStack();
    }
}
