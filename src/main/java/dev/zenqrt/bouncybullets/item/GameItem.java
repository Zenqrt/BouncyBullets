package dev.zenqrt.bouncybullets.item;

import dev.zenqrt.bouncybullets.BouncyBulletsPlugin;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class GameItem {

    private static final NamespacedKey ITEM_KEY = new NamespacedKey(BouncyBulletsPlugin.getInstance(), "game_item");

    private final String key;
    private final Component displayName;
    private final DataComponentsBuilder dataComponentsBuilder;
    private final List<Component> description;
    protected final Material material;

    protected GameItem(String key, Material material, Component displayName, List<Component> description) {
        this.key = key;
        this.material = material;
        this.displayName = displayName;
        this.description = description;
        this.dataComponentsBuilder = new DataComponentsBuilder();
    }

    protected GameItem(String key, Material material, Component displayName, List<Component> description, DataComponentsBuilder builder) {
            this.key = key;
            this.material = material;
            this.dataComponentsBuilder = builder;
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

    @SuppressWarnings("UnstableApiUsage")
    public ItemStack buildItemStack() {
        ItemStack itemStack = new ItemStack(material);

        List<DataComponentEntry<?>> dataComponentEntries = this.dataComponentsBuilder.build();
        dataComponentEntries.forEach(entry -> entry.applyTo(itemStack));

        itemStack.setData(DataComponentTypes.CUSTOM_NAME, this.displayName.decoration(TextDecoration.ITALIC, false));
        itemStack.setData(DataComponentTypes.LORE, ItemLore.lore(
                this.description.stream()
                        .map(component -> component.decoration(TextDecoration.ITALIC, false))
                        .toList()
        ));

        itemStack.editPersistentDataContainer(dataContainer -> dataContainer.set(ITEM_KEY, PersistentDataType.STRING, this.key));
        return itemStack;
    }

    public Material getMaterial() {
        return material;
    }

    public String getKey() {
        return key;
    }

    protected static DataComponentsBuilder dataComponentsBuilder() {
        return new DataComponentsBuilder();
    }

    public static class DataComponentsBuilder {

        private final List<DataComponentEntry<?>> entries = new ArrayList<>();

        private DataComponentsBuilder() {}

        @SuppressWarnings("UnstableApiUsage")
        public <T> DataComponentsBuilder addData(DataComponentType.Valued<T> valued, T value) {
            this.entries.add(
                    new DataComponentEntry<>(valued, value)
            );

            return this;
        }

        List<DataComponentEntry<?>> build() {
            return Collections.unmodifiableList(entries);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private record DataComponentEntry<T>(DataComponentType.Valued<T> valued, T value) {

        void applyTo(ItemStack itemStack) {
            itemStack.setData(this.valued, this.value);
        }

    }

}
