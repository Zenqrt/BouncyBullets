package dev.zenqrt.bouncybullets.item.items;

import dev.zenqrt.bouncybullets.game.event.PaperEventListener;
import dev.zenqrt.bouncybullets.gui.ClassSelectGui;
import dev.zenqrt.bouncybullets.item.GameItem;
import dev.zenqrt.bouncybullets.player.GamePlayerList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Collections;

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
                    new ClassSelectGui(players.get(player.getUniqueId()), players).show(player);
                })
                .build());

    }

}
