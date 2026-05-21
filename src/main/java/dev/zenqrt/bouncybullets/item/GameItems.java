package dev.zenqrt.bouncybullets.item;

import dev.zenqrt.bouncybullets.item.items.abilities.BulletSpreadAbilityItem;
import dev.zenqrt.bouncybullets.item.items.abilities.FullHealAbilityItem;
import dev.zenqrt.bouncybullets.item.items.abilities.InvisibilityAbilityItem;
import dev.zenqrt.bouncybullets.item.items.abilities.RailgunAbilityItem;
import dev.zenqrt.bouncybullets.item.items.guns.*;
import dev.zenqrt.bouncybullets.loadout.gun.BulletProperties;
import dev.zenqrt.bouncybullets.loadout.gun.GunProperties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class GameItems {

    private static final Map<String, GameItem> REGISTRY = new HashMap<>();
    private static final Map<String, GunItem> GUNS = new HashMap<>();

    public static final LoadoutGameItem LOADOUT = registerItem(new LoadoutGameItem());
    
    public static final DesertEagleGunItem DESERT_EAGLE = registerGun(
            new DesertEagleGunItem(
                    new GunProperties(5, 10, 0.05, 0.02, 6, 10, 2),
                    new BulletProperties(3, 500, -0.1F, 1, 7, 0.1F, 25, 4)
            )
    );
    public static final GrenadeLauncherGunItem GRENADE_LAUNCHER = registerGun(
            new GrenadeLauncherGunItem(
                    new GunProperties(5, 40, 0.1, 0.02, 6, 20, 2),
                    new BulletProperties(3, 1.25, 0, 8, 8, 0, 100, 0)
            )
    );
    public static final PistolGunItem PISTOL = registerGun(
            new PistolGunItem(
                    new GunProperties(2, 5, 0.05, 0.02, 8, 5, 2),
                    new BulletProperties(3, 200, -0.10F, 1, 5, 0.1F, 25, 5)
            )
    );
    public static final SilencedPistolGunItem SILENCED_PISTOL = registerGun(
            new SilencedPistolGunItem(
                    new GunProperties(2, 5, 0.05, 0.02, 8, 5, 2),
                    new BulletProperties(3, 200, -0.10F, 1, 5, 0.1F, 25, 5)
            )
    );
    public static final SMGGunItem SMG = registerGun(
            new SMGGunItem(
                    new GunProperties(3, 1, 0.05, 0.02, 32, 2, 2),
                    new BulletProperties(2, 350, -0.2F, 0.25, 1, 0.2F, 15, 2.5)
            )
    );
    public static final SniperRifleGunItem SNIPER_RIFLE = registerGun(
            new SniperRifleGunItem(
                    new GunProperties(5, 20, 0.1, 0.005, 3, 20, 5),
                    new BulletProperties(2, 1000, -0.5F, 8, 8, -0.5F, 100, 0)
            )
    );
    public static final TwinPistolGunItem TWIN_PISTOL = registerGun(
            new TwinPistolGunItem(
                    new GunProperties(5, 3, 0.05, 0.02, 16, 5, 2),
                    new BulletProperties(3, 200, -0.1F, 1, 5, 0.1F, 25, 5)
            )
    );
    
    public static final FullHealAbilityItem FULL_HEAL = registerItem(new FullHealAbilityItem());
    public static final RailgunAbilityItem RAILGUN = registerItem(new RailgunAbilityItem());
    public static final InvisibilityAbilityItem INVISIBILITY = registerItem(new InvisibilityAbilityItem());
    public static final BulletSpreadAbilityItem BULLET_SPREAD = registerItem(new BulletSpreadAbilityItem());

    private static <T extends GunItem> T registerGun(T gunItem) {
        GUNS.put(gunItem.getKey(), gunItem);

        return registerItem(gunItem);
    }

    private static <T extends GameItem> T registerItem(T gameItem) {
        REGISTRY.put(gameItem.getKey(), gameItem);

        return gameItem;
    }

    public static Map<String, GameItem> getAllItems() {
        return Collections.unmodifiableMap(REGISTRY);
    }

    public static Map<String, GunItem> getGuns() {
        return Collections.unmodifiableMap(GUNS);
    }

    private GameItems() {
    }

}
