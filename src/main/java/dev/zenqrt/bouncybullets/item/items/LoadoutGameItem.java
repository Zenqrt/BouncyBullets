package dev.zenqrt.bouncybullets.item.items;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import dev.zenqrt.bouncybullets.game.event.impl.PaperEventListener;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletPlayer;
import dev.zenqrt.bouncybullets.game.games.kit.PlayerClasses;
import dev.zenqrt.bouncybullets.item.GameItem;
import dev.zenqrt.bouncybullets.player.GamePlayerList;
import dev.zenqrt.bouncybullets.utils.AdventureUtils;
import dev.zenqrt.bouncybullets.utils.ComponentSplitting;
import dev.zenqrt.bouncybullets.utils.GuiUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public final class LoadoutGameItem extends GameItem {

    private final GamePlayerList players;

    public LoadoutGameItem(GamePlayerList players) {
        super("loadout", Material.NETHER_STAR,
                Component.text("Loadout", NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false),
                Collections.emptyList());

        this.players = players;
    }

    @Override
    public void registerEvents() {
        this.eventNode.registerListener(PaperEventListener.builder(PlayerInteractEvent.class)
                .filter(event -> filterGameItem(event.getItem(), this))
                .handler(event -> {
                    Player player = event.getPlayer();
                    new ClassSelectGui(players.get(player.getUniqueId())).show(player);
                })
                .build());

    }

    private class ClassSelectGui extends ChestGui {

        private final BouncyBulletPlayer player;

        public ClassSelectGui(BouncyBulletPlayer player) {
            super(3, "Class Selection");

            this.player = player;
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

                    List<Component> lore = new ArrayList<>(wrapDescription(playerClass, 30));
                    lore.add(Component.empty());
                    lore.add(AdventureUtils.withoutItalics("Click to select", NamedTextColor.YELLOW));

                    meta.lore(lore);
                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                });

                if (player.loadout().playerClass() == playerClass.getPlayerClass()) {
                    icon.editMeta(meta -> {
                        List<Component> lore = new ArrayList<>(wrapDescription(playerClass, 30));
                        lore.add(Component.empty());
                        lore.add(AdventureUtils.withoutItalics("Selected", NamedTextColor.GREEN));

                        meta.lore(lore);
                        meta.addEnchant(Enchantment.DURABILITY, 1, true);
                        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    });
                }

                pane.addItem(new GuiItem(playerClass.getIcon(), event -> {
                    event.setCancelled(true);
                    players.updatePlayer(player.uuid(), player -> player.withPlayerClass(playerClass.getPlayerClass()));
                    player.player().closeInventory();

                    player.player().sendMessage(Component.text("You have selected the ", NamedTextColor.GREEN)
                            .append(Component.text(playerClass.getPlayerClass().getName(), NamedTextColor.YELLOW))
                            .append(Component.text(" class!", NamedTextColor.GREEN)));
                }));
            }

            this.addPane(pane);
        }

        private static List<Component> wrapDescription(PlayerClasses playerClass, int wordWrapLength) {
            List<String> description = playerClass.getDescription();

//            Text
            MiniMessage miniMessage = MiniMessage.builder().preProcessor(string -> {
                System.out.println("Description: " + string);
                if (string.length() > wordWrapLength) {
                    return AdventureUtils.wrapTextIgnoringTags(string, wordWrapLength);
                }

                return string;
            }).build();


            List<Component> components = new ArrayList<>();

            description.forEach(string -> {
                Component component = miniMessage.deserialize(string).decoration(TextDecoration.ITALIC, false);
                System.out.println("Text: " + ((TextComponent) component).content());
                components.addAll(ComponentSplitting.split(component, Pattern.compile("\n")));
            });

            return components;
        }



    }
}
