package dev.zenqrt.bouncybullets.item;

import dev.zenqrt.bouncybullets.BouncyBullets;
import dev.zenqrt.bouncybullets.game.event.impl.PaperEventListener;
import dev.zenqrt.bouncybullets.game.event.impl.PaperEventNode;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class GameItem {

    private static final NamespacedKey ITEM_KEY = new NamespacedKey(BouncyBullets.getInstance(), "game_item");

    private final String key;
    private final Material material;
    private final Component displayName;
    private final List<Component> description;

    public GameItem(String key, Material material, Component displayName, List<Component> description) {
        this.key = key;
        this.material = material;
        this.displayName = displayName;
        this.description = description;
    }

    public static void registerGameItemEvents(PaperEventNode<Event> eventNode, List<GameItem> gameItems) {
        Map<String, GameItem> gameItemMap = gameItems.stream()
                        .collect(Collectors.toMap(GameItem::getKey, Function.identity()));

        eventNode.registerListener(PaperEventListener.builder(PlayerInteractEvent.class)
                .filter(event -> event.getItem() != null && event.getItem().hasItemMeta())
                .handler(event -> {
                    PersistentDataContainer dataContainer = Objects.requireNonNull(event.getItem()).getItemMeta().getPersistentDataContainer();

                    if (dataContainer.has(ITEM_KEY)) {
                        GameItem gameItem = gameItemMap.get(dataContainer.get(ITEM_KEY, PersistentDataType.STRING));

                        if (gameItem != null) {
                            gameItem.onInteract(event);
                        }
                    }
                }).build());
    }

    public abstract void onInteract(PlayerInteractEvent event);

    public ItemStack buildItemStack() {
        ItemStack itemStack = new ItemStack(material);
        itemStack.editMeta(meta -> {
            meta.displayName(displayName);
            meta.lore(description);
            meta.getPersistentDataContainer().set(ITEM_KEY, PersistentDataType.STRING, key);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        });

        return itemStack;
    }

    public String getKey() {
        return key;
    }
}
