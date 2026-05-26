package dev.zenqrt.bouncybullets.gui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import dev.zenqrt.bouncybullets.item.items.abilities.ActiveAbilityItem;
import dev.zenqrt.bouncybullets.item.items.guns.GunItem;
import dev.zenqrt.bouncybullets.loadout.Loadout;
import dev.zenqrt.bouncybullets.loadout.kit.PlayerClassType;
import dev.zenqrt.bouncybullets.utils.AdventureUtils;
import dev.zenqrt.bouncybullets.utils.GuiUtils;
import dev.zenqrt.bouncybullets.utils.ItemUtils;
import dev.zenqrt.bouncybullets.utils.MiniMessageUtils;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ClassSelectGui extends ChestGui {

    private final BouncyBulletGamePlayer gamePlayer;

    public ClassSelectGui(BouncyBulletGamePlayer gamePlayer) {
        super(3, "Class Selection");

        this.gamePlayer = gamePlayer;

        this.setOnGlobalClick(event -> event.setCancelled(true));

        this.addPane(Slot.fromXY(0, 0), GuiUtils.createBackgroundPane(getRows()));
        displayClasses();
    }

    @SuppressWarnings("UnstableApiUsage")
    private void displayClasses() {
        OutlinePane pane = new OutlinePane(7, 1);

        for (PlayerClassType playerClass : PlayerClassType.values()) {
            ItemStack icon = ItemUtils.clone(playerClass.getIcon());

            icon.setData(
                    DataComponentTypes.TOOLTIP_DISPLAY,
                    TooltipDisplay.tooltipDisplay()
                            .addHiddenComponents(
                                    DataComponentTypes.POTION_CONTENTS
                            )
                            .build()
            );
            icon.editMeta(meta -> {
                meta.displayName(AdventureUtils.withoutItalics(playerClass.getPlayerClass().getName(), NamedTextColor.GREEN));

                List<Component> lore = buildClassInformationLore(playerClass);

                lore.add(Component.empty());

                if (gamePlayer.getLoadout().playerClass() == playerClass.getPlayerClass()) {
                    meta.displayName(Objects.requireNonNull(meta.displayName()).decorate(TextDecoration.BOLD));
                    meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    lore.add(AdventureUtils.withoutItalics("Selected", NamedTextColor.GREEN));
                } else {
                    lore.add(AdventureUtils.withoutItalics("Left-click to select", NamedTextColor.GREEN));
                    lore.add(AdventureUtils.withoutItalics("Right-click for more info", NamedTextColor.YELLOW));
                }

                meta.lore(lore);
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            });

            pane.addItem(new ButtonItem(icon, UISounds.BUTTON_CLICK, UISounds.SUCCESS, event -> {
                event.setCancelled(true);

                if (event.isLeftClick()) {
                    this.gamePlayer.setLoadout(new Loadout(playerClass.getPlayerClass()));

                    Player player = this.gamePlayer.getPlayer();

                    player.closeInventory();
                    player.sendMessage(Component.text("You have selected the ", NamedTextColor.GREEN)
                            .append(Component.text(playerClass.getPlayerClass().getName(), NamedTextColor.YELLOW))
                            .append(Component.text(" class!", NamedTextColor.GREEN)));
                } else {
                    new ClassInfoGui(this, playerClass).show(event.getWhoClicked());
                }
            }));
        }

        this.addPane(Slot.fromXY(1, 1), pane);
    }

    private static List<Component> buildClassInformationLore(PlayerClassType playerClass) {
        List<Component> lore = new ArrayList<>(playerClass.getItemContents().stream()
                .map(component -> Component.text("• ", NamedTextColor.DARK_GRAY).append(component))
                .map(component -> component.decoration(TextDecoration.ITALIC, false))
                .toList());
        lore.add(Component.empty());
        lore.addAll(buildClassDescriptionLore(playerClass));

        return lore;
    }

    private static List<Component> buildClassDescriptionLore(PlayerClassType playerClass) {
        return MiniMessageUtils.wordWrapLore(playerClass.getDescription(), 30).stream()
                .map(component -> component.decoration(TextDecoration.ITALIC, false))
                .toList();
    }

    private static class ClassInfoGui extends ChestGui {

        private final ChestGui previousGui;
        private final PlayerClassType classType;

        public ClassInfoGui(ChestGui previousGui, PlayerClassType classType) {
            super(5, classType.getPlayerClass().getName() + " Class Info");

            this.previousGui = previousGui;
            this.classType = classType;

            this.setOnGlobalClick(event -> event.setCancelled(true));
            this.addPane(Slot.fromXY(0, 0), GuiUtils.createBackgroundPane(getRows()));
            addBackButton();
            displayInfo();
        }

        private void addBackButton() {
            StaticPane pane = new StaticPane(1, 1);

            ItemStack backItem = new ItemStack(Material.ARROW);
            backItem.editMeta(meta -> meta.displayName(AdventureUtils.withoutItalics("Back", NamedTextColor.GREEN)));

            pane.addItem(new ButtonItem(backItem, event -> {
                event.setCancelled(true);

                previousGui.show(event.getWhoClicked());
            }), 0, 0);

            this.addPane(Slot.fromXY(0, this.getRows() - 1), pane);
        }

        @SuppressWarnings("UnstableApiUsage")
        private void displayInfo() {
            StaticPane classPane = new StaticPane(1, 1);

            ItemStack icon = ItemUtils.clone(classType.getIcon());
            icon.setData(
                    DataComponentTypes.TOOLTIP_DISPLAY,
                    TooltipDisplay.tooltipDisplay()
                            .addHiddenComponents(
                                    DataComponentTypes.POTION_CONTENTS
                            )
                            .build()
            );
            icon.editMeta(meta -> {
                meta.displayName(AdventureUtils.withoutItalics(classType.getPlayerClass().getName(), NamedTextColor.GREEN));
                meta.lore(buildClassInformationLore(classType));

                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            });
            classPane.addItem(new GuiItem(icon), 0, 0);

            OutlinePane itemsPane = new OutlinePane(7, 1);
            itemsPane.setGap(1);
            itemsPane.align(OutlinePane.Alignment.CENTER);

            for (GunItem gun : this.classType.getPlayerClass().getGuns()) {
                itemsPane.addItem(new GuiItem(gun.buildItemStack(), event -> event.setCancelled(true)));
            }

            for (ActiveAbilityItem ability : this.classType.getPlayerClass().getActiveAbilities()) {
                itemsPane.addItem(new GuiItem(ability.buildItemStack(), event -> event.setCancelled(true)));
            }

            this.addPane(Slot.fromXY(4, 0), classPane);
            this.addPane(Slot.fromXY(1, 2), itemsPane);
        }
    }

}
