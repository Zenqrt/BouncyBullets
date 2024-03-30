package dev.zenqrt.bouncybullets.item.items;

import dev.zenqrt.bouncybullets.game.event.impl.PaperEventListener;
import dev.zenqrt.bouncybullets.item.GameItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Collections;

public final class LoadoutGameItem extends GameItem {

    public LoadoutGameItem() {
        super("loadout", Material.NETHER_STAR,
                Component.text("Loadout", NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false),
                Collections.emptyList());
    }

    @Override
    public void registerEvents() {
        this.eventNode.registerListener(PaperEventListener.builder(PlayerInteractEvent.class)
                .filter(event -> filterGameItem(event.getItem(), this))
                .handler(this::onInteract)
                .build());

    }

    public void onInteract(PlayerInteractEvent event) {
        System.out.println("Wow! I'm supposed to do something!");
    }
}
