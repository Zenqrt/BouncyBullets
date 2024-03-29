package dev.zenqrt.bouncybullets.item.items;

import dev.zenqrt.bouncybullets.item.GameItem;
import dev.zenqrt.bouncybullets.map.GameMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Collections;
import java.util.Map;

public final class VoteMapGameItem extends GameItem {

    private final Map<GameMap, Integer> mapVotes;

    public VoteMapGameItem(Map<GameMap, Integer> mapVotes) {
        super("vote_map", Material.BELL,
                Component.text("Vote Map", NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false),
                Collections.emptyList());

        this.mapVotes = mapVotes;
    }

    @Override
    public void onInteract(PlayerInteractEvent event) {
        System.out.println("Wow! I am also supposed to do something!");
    }
}
