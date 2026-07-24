package dev.zenqrt.bouncybullets.utils.atlas;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.object.ObjectContents;
import org.bukkit.NamespacedKey;

public record AtlasSpriteKey(Key atlasKey, Key spriteKey) {

    public static AtlasSpriteKey item(String key) {
        return new AtlasSpriteKey(Atlases.ITEMS, NamespacedKey.minecraft("item/" + key));
    }

    public static AtlasSpriteKey particle(String key) {
        return new AtlasSpriteKey(Atlases.PARTICLES, NamespacedKey.minecraft(key));
    }

    public static AtlasSpriteKey block(String key) {
        return new AtlasSpriteKey(Atlases.BLOCKS, NamespacedKey.minecraft("block/" + key));
    }
}
