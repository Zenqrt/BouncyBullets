package dev.zenqrt.bouncybullets.item.items;

import dev.zenqrt.bouncybullets.game.games.BulletProperties;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

public final class PistolGunItem extends GunItem {

    private static final BulletProperties PROPERTIES = new BulletProperties(3, 24, -0.10F, 3, -0.1F);

    public PistolGunItem() {
        super("pistol", Material.GOLDEN_HOE, Component.text("BB (Boing Boing) Pistol", NamedTextColor.YELLOW), PROPERTIES);
    }

}
