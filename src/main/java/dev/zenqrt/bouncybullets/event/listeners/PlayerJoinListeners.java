package dev.zenqrt.bouncybullets.event.listeners;

import dev.zenqrt.bouncybullets.game.GameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public final class PlayerJoinListeners implements Listener {

    private final GameManager gameManager;

    public PlayerJoinListeners(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.joinMessage(null);

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.quitMessage(null);

        UUID uuid = event.getPlayer().getUniqueId();

        if (!this.gameManager.isInGame(uuid))
            return;

        this.gameManager.tryLeaveGame(uuid);
    }

}
