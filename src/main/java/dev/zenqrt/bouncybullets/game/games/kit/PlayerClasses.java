package dev.zenqrt.bouncybullets.game.games.kit;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public enum PlayerClasses {
    STEALTH(Material.IRON_SWORD, new StealthPlayerClass(), List.of(
            "<dark_gray>• <gray>SMG",
            "<dark_gray>• <gray>Silenced Pistol",
            "",
            "<gold>Invisibility Cloak:",
            "<gray>Become invisible for <green>5 <gray>seconds.",
            "",
            "<gold>Passive Ability:",
            "<gray>Upon killing a player, receive a <aqua>Speed II <gray>effect for <green>5 <gray>seconds."
    ));

    private final PlayerClass playerClass;
    private final ItemStack icon;
    private final List<String> description;

    PlayerClasses(ItemStack icon, PlayerClass playerClass, List<String> description) {
        this.playerClass = playerClass;
        this.icon = icon;
        this.description = description;
    }

    PlayerClasses(Material material, PlayerClass playerClass, List<String> description) {
        this(new ItemStack(material), playerClass, description);
    }

    public PlayerClass getPlayerClass() {
        return playerClass;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public List<String> getDescription() {
        return description;
    }
}
