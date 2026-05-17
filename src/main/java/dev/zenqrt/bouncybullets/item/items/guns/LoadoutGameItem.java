package dev.zenqrt.bouncybullets.item.items.guns;

import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import dev.zenqrt.bouncybullets.gui.ClassSelectGui;
import dev.zenqrt.bouncybullets.item.GameItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;

public final class LoadoutGameItem extends GameItem {

    public LoadoutGameItem() {
        super("loadout", Material.NETHER_STAR,
                Component.text("Loadout", NamedTextColor.YELLOW)
                        .decoration(TextDecoration.ITALIC, false),
                Collections.emptyList());
    }

    @Override
    public void onInteract(BouncyBulletGame game, Player player, ItemStack itemStack, PlayerInteractEvent event) {
        BouncyBulletGamePlayer gamePlayer = game.findPlayer(player.getUniqueId());

        new ClassSelectGui(gamePlayer)
                .show(player);
    }
}
