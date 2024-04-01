package dev.zenqrt.bouncybullets.game.games;

import dev.zenqrt.bouncybullets.game.games.kit.PlayerClass;
import org.bukkit.inventory.PlayerInventory;

public record Loadout(PlayerClass playerClass) {

    public void giveItems(PlayerInventory inventory) {
        playerClass.getItems().forEach(inventory::setItem);
    }

}
