package dev.zenqrt.bouncybullets.event.listeners;

import dev.zenqrt.bouncybullets.lobby.LobbyInstance;
import dev.zenqrt.bouncybullets.player.PlayerSessionManager;
import dev.zenqrt.bouncybullets.stats.PlayerStatsManager;
import io.papermc.paper.event.player.AsyncPlayerSpawnLocationEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
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

    private final LobbyInstance lobby;
    private final PlayerStatsManager statsManager;
    private final PlayerSessionManager sessionManager;

    public PlayerJoinListeners(LobbyInstance lobby, PlayerStatsManager statsManager, PlayerSessionManager sessionManager) {
        this.lobby = lobby;
        this.statsManager = statsManager;
        this.sessionManager = sessionManager;
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
        event.setSpawnLocation(this.lobby.getSpawn());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.joinMessage(null);

        this.sessionManager.joinLobby(
                event.getPlayer(),
                false
        );
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.quitMessage(null);

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        this.sessionManager.tryLeaveLobby(player);
        this.sessionManager.tryLeaveGame(uuid);
        this.statsManager.trySave(uuid)
                .thenRun(() -> this.statsManager.removeStatsFromCache(uuid));
    }

}
