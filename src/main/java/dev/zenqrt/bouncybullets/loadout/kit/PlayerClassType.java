package dev.zenqrt.bouncybullets.loadout.kit;

import dev.zenqrt.bouncybullets.utils.MiniMessageUtils;
import dev.zenqrt.bouncybullets.utils.atlas.AtlasSpriteKey;
import dev.zenqrt.bouncybullets.utils.atlas.Atlases;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public enum PlayerClassType {
    STEALTH(new StealthPlayerClass(), Material.IRON_HORSE_ARMOR, AtlasSpriteKey.item("ominous_bottle"),
            List.of(
                    Component.text("SMG", NamedTextColor.GRAY),
                    Component.text("Silenced Pistol", NamedTextColor.GRAY),
                    Component.text("Invisibility Cloak", NamedTextColor.LIGHT_PURPLE)
            ),
            createPassiveDescriptions(
                    "Out of Sight", "While sneaking, your stealth meter fills up. Once the meter is filled, you gain <white>Invisibility<gray>. Running, shooting, or taking damage will reveal yourself.",
                    "Surprise Attack", "After emerging from being invisible, gain <aqua>Speed II<gray> for <green>5s<gray>."
            )
    ),
    HEALER(new HealerPlayerClass(), Material.SPLASH_POTION, AtlasSpriteKey.particle("goldheart_2"), meta -> ((PotionMeta) meta).setBasePotionType(PotionType.HEALING),
            List.of(
                    Component.text("BB-Pistol", NamedTextColor.GRAY),
                    Component.text("Miracle", NamedTextColor.LIGHT_PURPLE)
            ),
            createPassiveDescriptions(
                    "Blessing", "Regenerage <red>1❤<gray> every <green>2s<gray>."
            )
    ),
    SNIPER(new SniperPlayerClass(), Material.ENDER_EYE, AtlasSpriteKey.item("ender_eye"),
            List.of(
                    Component.text("Sniper Rifle", NamedTextColor.GRAY),
                    Component.text("Pistol", NamedTextColor.GRAY)
            ),
            createPassiveDescriptions(
                    "Concentration", "For each second of standing still, accumulate a <red>5%<gray> damage increase, up to <red>50%<gray> maximum. This is shown in the experience bar."
            )
    ),
    DEMOMAN(new DemomanPlayerClass(), Material.TNT, AtlasSpriteKey.block("tnt_side"),
            List.of(
                    Component.text("Grenade Launcher", NamedTextColor.GRAY),
                    Component.text("Desert Eagle", NamedTextColor.GRAY),
                    Component.text("Pocket Railgun", NamedTextColor.LIGHT_PURPLE)
            ),
            createPassiveDescriptions(
                    "Sluggish", "Your speed is decreased by some amount."
            )
    ),
    WINGMAN(new WingmanPlayerClass(), Material.ELYTRA, AtlasSpriteKey.item("elytra"),
            List.of(
                    Component.text("Twin Pistols", NamedTextColor.GRAY),
                    Component.text("Bullet Spread", NamedTextColor.LIGHT_PURPLE)
            ),
            createPassiveDescriptions(
                    "Wing's Thrust", "Gain a charged double jump every <green>10s<gray>, up to a max level of <aqua>5<gray>. Each level launches you further.",
                    "idk what to call this", "Any shots fired while flying above <green>2 blocks<gray> does <red>50%<gray> more damage."
            )
    ),
    HEAVY(new HeavyPlayerClass(), Material.IRON_CHESTPLATE, AtlasSpriteKey.item("iron_chestplate"),
            List.of(
                    Component.text("Minigun", NamedTextColor.GRAY),
                    Component.text("Bulletproof", NamedTextColor.LIGHT_PURPLE)
            ),
            createPassiveDescriptions(
                    "Tank", "Gain an extra row of <red>20<gray>.",
                    "Sluggish", "Your speed is decreased by some amount.",
                    "Undefended", "You take <red>+100% <gray>more damage while you are reloading or firing."
            )
    )
//    SIDEWINDER(new SidewinderPlayerClass(), Material.FEATHER,
//            List.of(),
//            List.of()
//    )
    ;

    private static List<Component> createPassiveDescriptions(String... strings) {
        List<Component> components = new ArrayList<>();

        for (int i = 0; i < strings.length; i += 2) {
            String name = strings[i];
            String description = "<gray>" + strings[i + 1];

            components.add(
                    Component.text("Passive Ability: ", NamedTextColor.GOLD)
                            .append(Component.text(name, NamedTextColor.AQUA))
            );

            components.addAll(MiniMessageUtils.wordWrapLore(List.of(description), 30));

            if (i + 2 < strings.length) {
                components.add(Component.empty());
            }
        }

        return components;
    }

    public static final PlayerClassType[] VALUES = PlayerClassType.values();

    private final PlayerClass playerClass;
    private final ItemStack icon;
    private final AtlasSpriteKey hudIcon;
    private final List<Component> itemContents;
    private final List<Component> description;

    PlayerClassType(PlayerClass playerClass, Material material, AtlasSpriteKey hudIcon, Consumer<ItemMeta> itemMetaHandler, List<Component> itemContents, List<Component> description) {
        this.playerClass = playerClass;
        this.itemContents = itemContents;
        this.description = description;

        this.icon = new ItemStack(material);
        this.icon.editMeta(itemMetaHandler);

        this.hudIcon = hudIcon;
    }

    PlayerClassType(PlayerClass playerClass, Material material, AtlasSpriteKey hudIcon, List<Component> itemContents, List<Component> description) {
        this(playerClass, material, hudIcon, _ -> {}, itemContents, description);
    }

    public PlayerClass getPlayerClass() {
        return playerClass;
    }

    public ItemStack getSelectionUIIcon() {
        return icon;
    }

    public AtlasSpriteKey getHudIcon() {
        return hudIcon;
    }

    public List<Component> getItemContents() {
        return itemContents;
    }

    public List<Component> getDescription() {
        return description;
    }
}
