package dev.zenqrt.bouncybullets.loadout;

import dev.zenqrt.bouncybullets.loadout.kit.PlayerClass;
import org.bukkit.inventory.PlayerInventory;

public record Loadout(PlayerClass playerClass) {

    public void giveItems(PlayerInventory inventory) {
        playerClass.getItems().forEach(inventory::setItem);
    }

}
