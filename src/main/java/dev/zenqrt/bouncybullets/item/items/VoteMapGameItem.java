package dev.zenqrt.bouncybullets.item.items;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.component.ToggleButton;
import dev.zenqrt.bouncybullets.item.GameItem;
import dev.zenqrt.bouncybullets.map.GameMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class VoteMapGameItem extends GameItem {

    private final Map<GameMap, Integer> mapVotes;
    private final Map<UUID, GameMap> selectedMaps = new HashMap<>();

    public VoteMapGameItem(Map<GameMap, Integer> mapVotes) {
        super("vote_map", Material.BELL,
                Component.text("Vote Map", NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false),
                Collections.emptyList());

        this.mapVotes = mapVotes;
    }

    @Override
    public void onInteract(PlayerInteractEvent event) {
        ChestGui voteGui = new ChestGui(3, "Vote Map");
        voteGui.setOnGlobalClick(clickEvent -> clickEvent.setCancelled(true));

        OutlinePane pane = new OutlinePane(0, 0, 7, 1);

        mapVotes.forEach((gameMap, votes) -> {
            GuiItem guiItem = new GuiItem(createMapItemStack(event.getPlayer().getUniqueId(), gameMap));

            if (selectedMaps.get(event.getPlayer().getUniqueId()) != gameMap) {
                guiItem.setAction(click -> {
                    selectedMaps.put(event.getPlayer().getUniqueId(), gameMap);
                    voteGui.update();
                });
            } else {
                guiItem.setAction(click -> event.getPlayer().sendMessage(Component.text("You have already voted for this map", NamedTextColor.RED)));
            }

            pane.addItem(guiItem);
        });

        voteGui.addPane(pane);
        voteGui.show(event.getPlayer());
    }

    private ItemStack createMapItemStack(UUID uuid, GameMap gameMap) {
        ItemStack itemStack = new ItemStack(Material.FILLED_MAP);
        itemStack.editMeta(meta -> {
            meta.displayName(Component.text(gameMap.worldFolder().getName(), NamedTextColor.YELLOW));

            if (selectedMaps.get(uuid) == gameMap) {
                meta.lore(Collections.singletonList(Component.text("Selected", NamedTextColor.GREEN)));
                meta.addEnchant(Enchantment.DURABILITY, 1, false);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
        });
        return itemStack;
    }
}
