package dev.zenqrt.bouncybullets.game.games.kit;

import org.bukkit.inventory.PlayerInventory;

public interface PlayerClass {
    String getName();

    void giveItems(PlayerInventory inventory);
}
