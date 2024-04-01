package dev.zenqrt.bouncybullets.game.games.kit;

import dev.zenqrt.bouncybullets.game.games.BouncyBulletPlayer;
import org.bukkit.inventory.ItemStack;

import java.util.TreeMap;

public interface PlayerClass {
    String getName();
    TreeMap<Integer, ItemStack> getItems();

    default void onStartUse(BouncyBulletPlayer player) {}
    default void onStopUse(BouncyBulletPlayer player) {}
}
