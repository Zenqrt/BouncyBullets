package dev.zenqrt.bouncybullets.item.items;

import dev.zenqrt.bouncybullets.game.event.impl.PaperEventListener;
import dev.zenqrt.bouncybullets.item.GameItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class ActiveAbilityItem extends GameItem {

    public ActiveAbilityItem(String key, Material material, Component displayName, List<Component> description, Consumer<ItemMeta> itemMetaHandler) {
        super(key, material, displayName, description, itemMetaHandler);
    }

    public abstract void onUse(PlayerInteractEvent event);

    @Override
    public void registerEvents() {
        this.eventNode.registerListener(PaperEventListener.builder(PlayerInteractEvent.class)
                .filter(event -> filterGameItem(event.getItem(), this))
                .filter(event -> event.getAction().isRightClick())
                .handler(event -> {
                    event.setCancelled(true);

                    if (event.getPlayer().hasCooldown(Objects.requireNonNull(event.getItem()).getType()))
                        return;

                    this.onUse(event);
                })
                .build());
    }
}
