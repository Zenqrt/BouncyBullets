package dev.zenqrt.bouncybullets.event.listeners;

import dev.zenqrt.bouncybullets.game.GameManager;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.item.GameItem;
import dev.zenqrt.bouncybullets.item.GameItems;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public final class GameItemListeners implements Listener {

    private final GameManager gameManager;

    public GameItemListeners(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Optional<BouncyBulletGame> gameOptional = this.gameManager.findPlayerGame(player.getUniqueId());

        if (gameOptional.isEmpty())
            return;

        ItemStack itemStack = event.getItem();

        if (itemStack == null)
            return;

        GameItem.findGameItemId(itemStack)
                .ifPresent(
                        gameItemId -> {
                            GameItem gameItem = GameItems.getAllItems().get(gameItemId);

                            gameItem.onInteract(gameOptional.get(), player, itemStack, event);
                        });
    }

}
