package dev.zenqrt.bouncybullets.event.listeners;

import dev.zenqrt.bouncybullets.game.GameManager;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import dev.zenqrt.bouncybullets.item.GameItem;
import dev.zenqrt.bouncybullets.item.GameItems;
import dev.zenqrt.bouncybullets.item.items.guns.GunItem;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public final class GunListeners implements Listener {

    private final GameManager gameManager;

    public GunListeners(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void displayAmmoItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        Optional<BouncyBulletGame> gameOptional = this.gameManager.findPlayerGame(player.getUniqueId());

        if (gameOptional.isEmpty())
            return;

        BouncyBulletGame game = gameOptional.get();
        BouncyBulletGamePlayer gamePlayer = game.findPlayer(player.getUniqueId());
        ItemStack itemStack = player.getInventory().getItem(event.getNewSlot());

        if (itemStack == null) {
            gamePlayer.getHud().hideAmmo();
            gamePlayer.getHud().updateHudText();
            return;
        }

        Optional<GunItem> gunItemOptional = GameItem.findGameItemId(itemStack)
                .map(gameId -> GameItems.getGuns().get(gameId));

        if (gunItemOptional.isEmpty()) {
            gamePlayer.getHud().hideAmmo();
            gamePlayer.getHud().updateHudText();
            return;
        }

        GunItem gunItem = gunItemOptional.get();

        int ammo = gunItem.getAmmo(itemStack);

        gamePlayer.getHud().updateAmmo(ammo, gunItem.getGunProperties().magazineSize());
        gamePlayer.getHud().updateHudText();
    }

    @EventHandler
    public void displayAmmoInventorySlotChange(PlayerInventorySlotChangeEvent event) {
        Player player = event.getPlayer();
        Optional<BouncyBulletGame> gameOptional = this.gameManager.findPlayerGame(player.getUniqueId());

        if (gameOptional.isEmpty())
            return;

        BouncyBulletGame game = gameOptional.get();
        BouncyBulletGamePlayer gamePlayer = game.findPlayer(player.getUniqueId());
        ItemStack itemStack = event.getNewItemStack();

        if (event.getSlot() != player.getInventory().getHeldItemSlot())
            return;

        Optional<GunItem> gunItemOptional = GameItem.findGameItemId(itemStack)
                .map(gameId -> GameItems.getGuns().get(gameId));

        if (gunItemOptional.isEmpty()) {
            gamePlayer.getHud().hideAmmo();
            gamePlayer.getHud().updateHudText();
            return;
        }

        GunItem gunItem = gunItemOptional.get();

        int ammo = gunItem.getAmmo(itemStack);

        gamePlayer.getHud().updateAmmo(ammo, gunItem.getGunProperties().magazineSize());
        gamePlayer.getHud().updateHudText();
    }

    @EventHandler
    public void reloadGun(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        Optional<BouncyBulletGame> gameOptional = this.gameManager.findPlayerGame(player.getUniqueId());

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
        BouncyBulletGamePlayer gamePlayer = game.findPlayer(player.getUniqueId());

        gunItem.reload(gamePlayer, player, itemStack);
    }

}
