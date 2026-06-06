package dev.zenqrt.bouncybullets.utils;

import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;

public interface Sounds extends Sound {

    Key GUN_DESERT_EAGLE_RELOAD = getCustomSound("gun.desert_eagle.reload");

    private static Key getCustomSound(String key) {
        return new NamespacedKey("bouncybullets", key);
    }

}
