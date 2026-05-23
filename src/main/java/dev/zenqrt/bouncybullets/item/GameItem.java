package dev.zenqrt.bouncybullets.item;

import dev.zenqrt.bouncybullets.BouncyBulletsPlugin;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class GameItem {

    private static final NamespacedKey ITEM_KEY = new NamespacedKey(BouncyBulletsPlugin.getInstance(), "game_item");

    private final String key;
    private final Component displayName;
    private final Consumer<ItemMeta> itemMetaHandler;
    private final List<Component> description;
    protected final Material material;

    public GameItem(String key, Material material, Component displayName, List<Component> description) {
        this.key = key;
        this.material = material;
        this.displayName = displayName;
        this.description = description;
        this.itemMetaHandler = _ -> {};
    }

    public GameItem(String key, Material material, Component displayName, List<Component> description, Consumer<ItemMeta> itemMetaHandler) {
        this.key = key;
        this.material = material;
        this.itemMetaHandler = itemMetaHandler;
        this.displayName = displayName;
        this.description = description;
    }

    public void onInteract(BouncyBulletGame game, Player player, ItemStack itemStack, PlayerInteractEvent event) {}
    public void onHeld(BouncyBulletGame game, Player player, ItemStack itemStack, ItemStack previousItemStack) {}
    public void onUnheld(BouncyBulletGame game, Player player, ItemStack itemStack, ItemStack newItemStack) {}

    public static Optional<String> findGameItemId(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null)
            return Optional.empty();

        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        String gameItemId = dataContainer.get(ITEM_KEY, PersistentDataType.STRING);

        return gameItemId == null ? Optional.empty() : Optional.of(gameItemId);
    }

    public ItemStack buildItemStack() {
        ItemStack itemStack = new ItemStack(material);
        itemStack.editMeta(meta -> {
            meta.displayName(displayName);
            meta.lore(description.stream()
                    .map(component -> component.decoration(TextDecoration.ITALIC, false))
                    .toList());
            meta.getPersistentDataContainer().set(ITEM_KEY, PersistentDataType.STRING, key);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        });
        itemStack.editMeta(itemMetaHandler);

        return itemStack;
    }

    public String getKey() {
        return key;
    }
}
