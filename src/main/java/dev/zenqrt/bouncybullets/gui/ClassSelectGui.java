package dev.zenqrt.bouncybullets.gui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletPlayer;
import dev.zenqrt.bouncybullets.game.games.kit.PlayerClass;
import dev.zenqrt.bouncybullets.game.games.kit.PlayerClasses;
import dev.zenqrt.bouncybullets.player.GamePlayerList;
import dev.zenqrt.bouncybullets.utils.AdventureUtils;
import dev.zenqrt.bouncybullets.utils.ComponentSplitting;
import dev.zenqrt.bouncybullets.utils.GuiUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public final class ClassSelectGui extends ChestGui {

    private final BouncyBulletPlayer player;
    private final GamePlayerList players;

    public ClassSelectGui(BouncyBulletPlayer player, GamePlayerList players) {
        super(3, "Class Selection");

        this.player = player;
        this.players = players;

        this.setOnGlobalClick(event -> event.setCancelled(true));

        this.addPane(GuiUtils.createBackgroundPane(getRows()));
        displayClasses();
    }

    private void displayClasses() {
        OutlinePane pane = new OutlinePane(1, 1, 7, 1);

        for (PlayerClasses playerClass : PlayerClasses.values()) {
            ItemStack icon = playerClass.getIcon();

            icon.editMeta(meta -> {
                meta.displayName(AdventureUtils.withoutItalics(playerClass.getPlayerClass().getName(), NamedTextColor.YELLOW));

                List<Component> lore = new ArrayList<>();
                playerClass.getPlayerClass().getItems().forEach((slot, item) -> lore.add(AdventureUtils.withoutItalics("• ", NamedTextColor.DARK_GRAY)
                        .append(Objects.requireNonNull(item.getItemMeta().displayName()))));
                lore.add(Component.empty());
                lore.addAll(wrapDescription(playerClass, 30));
                lore.add(Component.empty());

                if (player.loadout().playerClass() == playerClass.getPlayerClass()) {
                    meta.displayName(Objects.requireNonNull(meta.displayName()).decorate(TextDecoration.BOLD));
                    meta.addEnchant(Enchantment.DURABILITY, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    lore.add(AdventureUtils.withoutItalics("Selected", NamedTextColor.GREEN));
                } else {
                    lore.add(AdventureUtils.withoutItalics("Click to select", NamedTextColor.YELLOW));
                }

                meta.lore(lore);
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            });

            pane.addItem(new ButtonItem(playerClass.getIcon(), UISounds.BUTTON_CLICK, UISounds.SUCCESS, event -> {
                event.setCancelled(true);

                if (event.isLeftClick()) {
                    players.updatePlayer(player.uuid(), player -> player.withPlayerClass(playerClass.getPlayerClass()));
                    player.player().closeInventory();

                    player.player().sendMessage(Component.text("You have selected the ", NamedTextColor.GREEN)
                            .append(Component.text(playerClass.getPlayerClass().getName(), NamedTextColor.YELLOW))
                            .append(Component.text(" class!", NamedTextColor.GREEN)));
                } else {
                    new ClassInfoGui(this, playerClass.getPlayerClass()).show(event.getWhoClicked());
                }
            }));
        }

        this.addPane(pane);
    }

    private static List<Component> wrapDescription(PlayerClasses playerClass, int wordWrapLength) {
        List<String> description = playerClass.getDescription();

        MiniMessage miniMessage = MiniMessage.builder().preProcessor(string -> {
            if (string.length() > wordWrapLength) {
                return AdventureUtils.wrapTextIgnoringTags(string, wordWrapLength);
            }

            return string;
        }).build();


        List<Component> components = new ArrayList<>();

        description.forEach(string -> {
            Component component = miniMessage.deserialize(string).decoration(TextDecoration.ITALIC, false);
            components.addAll(ComponentSplitting.split(component, Pattern.compile("\n")));
        });

        return components;
    }

    private static class ClassInfoGui extends ChestGui {

        private final ChestGui previousGui;
        private final PlayerClass playerClass;

        public ClassInfoGui(ChestGui previousGui, PlayerClass playerClass) {
            super(5, playerClass.getName() + " Class Info");

            this.previousGui = previousGui;
            this.playerClass = playerClass;

            this.setOnGlobalClick(event -> event.setCancelled(true));
            this.addPane(GuiUtils.createBackgroundPane(getRows()));
            addBackButton();
            displayInfo();
        }

        private void addBackButton() {
            StaticPane pane = new StaticPane(0, this.getRows() - 1, 1, 1);

            ItemStack backItem = new ItemStack(Material.BARRIER);
            backItem.editMeta(meta -> meta.displayName(AdventureUtils.withoutItalics("Back", NamedTextColor.RED)));

            pane.addItem(new ButtonItem(backItem, event -> {
                event.setCancelled(true);

                previousGui.show(event.getWhoClicked());
            }), 0, 0);

            this.addPane(pane);
        }

        private void displayInfo() {
            OutlinePane itemsPane = new OutlinePane(1, 1, 7, 1);
            itemsPane.setGap(1);
            itemsPane.align(OutlinePane.Alignment.CENTER);

            playerClass.getItems().forEach((slot, item) -> itemsPane.addItem(new GuiItem(item.clone(), event -> event.setCancelled(true))));
            this.addPane(itemsPane);
        }
    }

}
