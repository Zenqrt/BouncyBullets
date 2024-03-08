package dev.zenqrt.bouncybullets.item.items;

import dev.zenqrt.bouncybullets.item.GameItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Collections;

public final class VoteMapGameItem extends GameItem {

    public VoteMapGameItem() {
        super("vote_map", Material.BELL,
                Component.text("Vote Map", NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false),
                Collections.emptyList());
    }

    @Override
    public void onInteract(PlayerInteractEvent event) {
        System.out.println("Wow! I am also supposed to do something!");
    }
}
