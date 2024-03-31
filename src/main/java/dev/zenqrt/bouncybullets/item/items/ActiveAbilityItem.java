package dev.zenqrt.bouncybullets.item.items;

import dev.zenqrt.bouncybullets.game.event.impl.PaperEventListener;
import dev.zenqrt.bouncybullets.item.GameItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

public abstract class ActiveAbilityItem extends GameItem {

    private final int cooldownTime;

    public ActiveAbilityItem(String key, Material material, Component displayName, List<Component> description, int cooldownTime) {
        super(key, material, displayName, description);

        this.cooldownTime = cooldownTime;
    }

    public abstract void onUse(PlayerInteractEvent event);

    @Override
    public void registerEvents() {
        this.eventNode.registerListener(PaperEventListener.builder(PlayerInteractEvent.class)
                .filter(event -> filterGameItem(event.getItem(), this))
                .filter(event -> event.getAction().isRightClick())
                .handler(event -> {
                    event.setCancelled(true);

                    this.onUse(event);
                    event.getPlayer().setCooldown(this.material, this.cooldownTime);
                })
                .build());
    }
}
