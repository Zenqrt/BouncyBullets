package dev.zenqrt.bouncybullets.item;

import dev.zenqrt.bouncybullets.BouncyBullets;
import dev.zenqrt.bouncybullets.game.event.impl.PaperEventNode;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class GameItem {

    private static final NamespacedKey ITEM_KEY = new NamespacedKey(BouncyBullets.getInstance(), "game_item");

    private final String key;
    private final Material material;
    private final Component displayName;
    private final List<Component> description;
    protected final PaperEventNode<Event> eventNode = new PaperEventNode<>();

    public GameItem(String key, Material material, Component displayName, List<Component> description) {
        this.key = key;
        this.material = material;
        this.displayName = displayName;
        this.description = description;
    }

    public static void registerGameItemEvents(List<GameItem> gameItems) {
        gameItems.forEach(GameItem::registerEvents);
    }

    public static boolean filterGameItem(@Nullable ItemStack itemStack, GameItem gameItem) {
        if (itemStack == null || !itemStack.hasItemMeta()) {
            return false;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null) {
            return false;
        }

        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();

        return dataContainer.has(ITEM_KEY) && dataContainer.get(ITEM_KEY, PersistentDataType.STRING).equals(gameItem.getKey());
    }

    public abstract void registerEvents();

    public void unregisterEvents() {
        this.eventNode.unregisterAllListeners();
    }

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
