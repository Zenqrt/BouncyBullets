package dev.zenqrt.bouncybullets.game.games.kit;

import org.bukkit.inventory.ItemStack;

import java.util.TreeMap;

public interface PlayerClass {
    String getName();
    TreeMap<Integer, ItemStack> getItems();
}
