package dev.zenqrt.bouncybullets.game.games.kit;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.List;
import java.util.function.Consumer;

public enum PlayerClasses {
    STEALTH(new StealthPlayerClass(), Material.IRON_HORSE_ARMOR,
            List.of(
                    Component.text("SMG", NamedTextColor.GRAY),
                    Component.text("Silenced Pistol", NamedTextColor.GRAY),
                    Component.text("Invisibility Cloak", NamedTextColor.LIGHT_PURPLE)
            ),
            List.of(
                    "<gold>Passive Ability:",
                    "<gray>Upon killing a player, receive a <aqua>Speed II <gray>effect for <green>5 <gray>seconds."
    )),
    HEALER(new HealerPlayerClass(), Material.SPLASH_POTION, meta -> {
        ((PotionMeta) meta).setBasePotionType(PotionType.INSTANT_HEAL);
        meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
    },
            List.of(
                    Component.text("Pistol", NamedTextColor.GRAY),
                    Component.text("Miracle", NamedTextColor.LIGHT_PURPLE)
            ),
            List.of(
                    "<gold>Passive Ability:",
                    "<gray>Regenerate <red>1❤</red> every <green>2 <gray>seconds."
            )),
    SNIPER(new SniperPlayerClass(), Material.ENDER_EYE, List.of(
            Component.text("Sniper Rifle", NamedTextColor.GRAY),
            Component.text("Pistol", NamedTextColor.GRAY)
    ),
            List.of(
                    "<gold>Passive Ability:",
                    "<gray>For each second of standing still, accumulate <red>1% <gray>damage increase, up to <red>50% <gray>maximum. This is shown in the experience bar."
            ));

    private final PlayerClass playerClass;
    private final ItemStack icon;
    private final List<Component> itemContents;
    private final List<String> description;

    PlayerClasses(PlayerClass playerClass, Material material, Consumer<ItemMeta> itemMetaHandler, List<Component> itemContents, List<String> description) {
        this.playerClass = playerClass;
        this.itemContents = itemContents;
        this.description = description;

        this.icon = new ItemStack(material);
        this.icon.editMeta(itemMetaHandler);
    }

    PlayerClasses(PlayerClass playerClass, Material material, List<Component> itemContents, List<String> description) {
        this(playerClass, material, meta -> {}, itemContents, description);
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
