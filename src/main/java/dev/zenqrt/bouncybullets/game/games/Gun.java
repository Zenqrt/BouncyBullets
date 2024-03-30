package dev.zenqrt.bouncybullets.game.games;

import dev.zenqrt.bouncybullets.item.items.GunItem;
import dev.zenqrt.bouncybullets.item.items.PistolGunItem;
import org.bukkit.inventory.ItemStack;

import java.util.function.Function;

public enum Gun {
    PISTOL(new GunProperties(250, 0.05, 0.02), new BulletProperties(3, 200, -0.10F, 3, 0.1F), PistolGunItem::new);

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
