package dev.zenqrt.bouncybullets.event.listeners;

import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import dev.zenqrt.bouncybullets.item.GameItem;
import dev.zenqrt.bouncybullets.item.GameItems;
import dev.zenqrt.bouncybullets.item.items.guns.GunItem;
import dev.zenqrt.bouncybullets.player.PlayerSessionManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public final class GunListeners implements Listener {

    private final PlayerSessionManager sessionManager;

    public GunListeners(PlayerSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @EventHandler
    public void reloadGun(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        Optional<BouncyBulletGame> gameOptional = this.sessionManager.findGameSession(player.getUniqueId());

        if (gameOptional.isEmpty())
            return;

        ItemStack itemStack = event.getOffHandItem();
        Optional<String> gameItemIdOptional = GameItem.findGameItemId(itemStack);

        if (gameItemIdOptional.isEmpty())
            return;

        GunItem gunItem = GameItems.getGuns().get(gameItemIdOptional.get());

        if (gunItem == null)
            return;

        BouncyBulletGame game = gameOptional.get();
        BouncyBulletGamePlayer gamePlayer = game.findPlayerOrThrow(player.getUniqueId());

        gamePlayer.stopAiming(game);
        gunItem.reload(gamePlayer, player, itemStack);
    }

}
