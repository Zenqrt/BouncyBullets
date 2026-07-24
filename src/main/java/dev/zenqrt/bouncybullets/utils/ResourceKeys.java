package dev.zenqrt.bouncybullets.utils;

import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;

public final class ResourceKeys {

    public static Key hudFont(String key) {
        return Key.key("bouncybullets", "hud/" + key);
    }

    private ResourceKeys() {
    }

}
