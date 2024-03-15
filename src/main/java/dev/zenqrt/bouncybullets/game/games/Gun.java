package dev.zenqrt.bouncybullets.game.games;

import dev.zenqrt.bouncybullets.item.items.GunItem;
import dev.zenqrt.bouncybullets.item.items.PistolGunItem;
import org.bukkit.inventory.ItemStack;

import java.util.function.Function;

public enum Gun {
    PISTOL(new BulletProperties(3, 200, -0.10F, 3, 0.1F), PistolGunItem::new);

    private final BulletProperties properties;
    private final GunItem item;

    Gun(BulletProperties properties, Function<BulletProperties, GunItem> itemFunction) {
        this.properties = properties;
        this.item = itemFunction.apply(properties);
    }

    public BulletProperties getProperties() {
        return properties;
    }

    public GunItem getItem() {
        return item;
    }

    public ItemStack buildItemStack() {
        return item.buildItemStack();
    }
}
