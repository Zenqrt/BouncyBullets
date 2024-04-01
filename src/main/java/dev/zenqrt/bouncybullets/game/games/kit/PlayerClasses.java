package dev.zenqrt.bouncybullets.game.games.kit;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public enum PlayerClasses {
    STEALTH(Material.IRON_SWORD, new StealthPlayerClass(),
            List.of(
                    Component.text("SMG", NamedTextColor.GRAY),
                    Component.text("Silenced Pistol", NamedTextColor.GRAY),
                    Component.text("Invisibility Cloak", NamedTextColor.LIGHT_PURPLE)
            ),
            List.of(
                    "<gold>Passive Ability:",
                    "<gray>Upon killing a player, receive a <aqua>Speed II <gray>effect for <green>5 <gray>seconds."
    ));

    private final PlayerClass playerClass;
    private final ItemStack icon;
    private final List<Component> itemContents;
    private final List<String> description;

    PlayerClasses(ItemStack icon, PlayerClass playerClass, List<Component> itemContents, List<String> description) {
        this.playerClass = playerClass;
        this.icon = icon;
        this.itemContents = itemContents;
        this.description = description;
    }

    PlayerClasses(Material material, PlayerClass playerClass, List<Component> itemContents, List<String> description) {
        this(new ItemStack(material), playerClass, itemContents, description);
    }

    public PlayerClass getPlayerClass() {
        return playerClass;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public List<Component> getItemContents() {
        return itemContents;
    }

    public List<String> getDescription() {
        return description;
    }
}
