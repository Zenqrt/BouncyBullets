package dev.zenqrt.bouncybullets.event.listeners;

import dev.zenqrt.bouncybullets.config.ServerConfig;
import dev.zenqrt.bouncybullets.game.GameManager;
import dev.zenqrt.bouncybullets.lobby.LobbyManager;
import dev.zenqrt.bouncybullets.stats.PlayerStatsManager;
import io.papermc.paper.event.player.AsyncPlayerSpawnLocationEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class PlayerJoinListeners implements Listener {

    private static final int LOAD_STATS_TIMEOUT_SECONDS = 10;

    private final PlayerStatsManager statsManager;
    private final GameManager gameManager;
    private final LobbyManager lobbyManager;
    private final ServerConfig config;

    public PlayerJoinListeners(PlayerStatsManager statsManager, GameManager gameManager, LobbyManager lobbyManager, ServerConfig config) {
        this.statsManager = statsManager;
        this.gameManager = gameManager;
        this.lobbyManager = lobbyManager;
        this.config = config;
    }

    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        try {
            this.statsManager.loadStatsAsync(event.getUniqueId())
                    .get(LOAD_STATS_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            event.disallow(
                    AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    Component.text("Failed to get player stats\n", NamedTextColor.RED)
                            .append(Component.text(e.getCause().toString(), NamedTextColor.WHITE))
            );
            throw new RuntimeException(e);
        }
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

        this.gameManager.tryLeaveGame(uuid);
        this.statsManager.trySave(uuid)
                .thenRun(() -> this.statsManager.removeStatsFromCache(uuid));
    }

}
