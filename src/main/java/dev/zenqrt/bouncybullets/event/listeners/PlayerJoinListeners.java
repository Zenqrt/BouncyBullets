package dev.zenqrt.bouncybullets.event.listeners;

import dev.zenqrt.bouncybullets.config.ServerConfig;
import dev.zenqrt.bouncybullets.game.GameManager;
import dev.zenqrt.bouncybullets.lobby.LobbyManager;
import io.papermc.paper.event.player.AsyncPlayerSpawnLocationEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public final class PlayerJoinListeners implements Listener {

    private final GameManager gameManager;
    private final LobbyManager lobbyManager;
    private final ServerConfig config;

    public PlayerJoinListeners(GameManager gameManager, LobbyManager lobbyManager, ServerConfig config) {
        this.gameManager = gameManager;
        this.lobbyManager = lobbyManager;
        this.config = config;
    }

    @EventHandler
    @SuppressWarnings("UnstableApiUsage")
    public void onPlayerSpawn(AsyncPlayerSpawnLocationEvent event) {
        event.setSpawnLocation(this.config.getLobbySpawn());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.joinMessage(null);

        this.lobbyManager.setupLobbyPlayer(event.getPlayer());
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
