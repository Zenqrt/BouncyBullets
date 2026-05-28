package dev.zenqrt.bouncybullets.stats;

import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import dev.zenqrt.bouncybullets.stats.database.PlayerStatsRepository;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class PlayerStatsManager {

    private final Map<UUID, PlayerStats> statsCache = new HashMap<>();
    private final Set<UUID> statsDirty = new HashSet<>();

    private final PlayerStatsRepository repository;
    private final Plugin plugin;

    public PlayerStatsManager(Plugin plugin, PlayerStatsRepository repository) {
        this.plugin = plugin;
        this.repository = repository;
    }

    public PlayerStats getStatsOrThrow(UUID uuid) {
        PlayerStats stats = this.statsCache.get(uuid);

        if (stats == null)
            throw new IllegalStateException("Stats not loaded for " + uuid);

        return stats;
    }

    public void saveDirty() {
        for (UUID uuid : this.statsDirty) {
            PlayerStats stats = getStatsOrThrow(uuid);

            this.repository.save(uuid, stats);
        }

        this.statsDirty.clear();
    }

    public boolean hasNoDirty() {
        return this.statsDirty.isEmpty();
    }

    public CompletableFuture<Void> loadStatsAsync(UUID uuid) {
        return this.repository.load(uuid)
                .thenAccept(optionalStats ->
                        Bukkit.getScheduler().runTask(
                                this.plugin,
                                () -> this.statsCache.put(
                                        uuid,
                                        optionalStats.orElseGet(() -> createNew(uuid)))
                ));
    }

    private PlayerStats createNew(UUID uuid) {
        markDirty(uuid);

        return new PlayerStats();
    }

    public void removeStatsFromCache(UUID uuid) {
        this.statsCache.remove(uuid);
    }

    public CompletableFuture<Boolean> trySave(UUID uuid) {
        if (!this.statsDirty.remove(uuid))
            return CompletableFuture.completedFuture(false);

        PlayerStats stats = getStatsOrThrow(uuid);

        return this.repository.save(uuid, stats)
                .thenApply(_ -> true);
    }

    private void markDirty(UUID uuid) {
        this.statsDirty.add(uuid);
    }

    private void recordStats(UUID uuid, Consumer<PlayerStats> updater) {
        PlayerStats stats = getStatsOrThrow(uuid);

        updater.accept(stats);

        markDirty(uuid);
    }

    public void recordKill(BouncyBulletGamePlayer gamePlayer) {
        recordStats(
                gamePlayer.getUuid(),
                stats -> {
                    stats.addKillToTotal();
                    stats.getClassStats(gamePlayer.getLoadout().classType())
                            .addKill();
                }
        );
    }

    public void recordDeath(BouncyBulletGamePlayer gamePlayer) {
        recordStats(
                gamePlayer.getUuid(),
                stats -> {
                    stats.addDeathToTotal();
                    stats.getClassStats(gamePlayer.getLoadout().classType())
                            .addDeath();
                }
        );
    }

    public void recordWin(BouncyBulletGamePlayer gamePlayer) {
        recordStats(
                gamePlayer.getUuid(),
                stats -> {
                    stats.addWinToTotal();
                    stats.getClassStats(gamePlayer.getLoadout().classType())
                            .addWin();
                }
        );
    }

    public void recordLoss(BouncyBulletGamePlayer gamePlayer) {
        recordStats(
                gamePlayer.getUuid(),
                stats -> {
                    stats.addLossToTotal();
                    stats.getClassStats(gamePlayer.getLoadout().classType())
                            .addLoss();
                }
        );
    }

    public void recordGamePlayed(BouncyBulletGamePlayer gamePlayer) {
        recordGamePlayed(gamePlayer.getUuid());
    }

    public void recordGamePlayed(UUID uuid) {
        recordStats(
                uuid,
                PlayerStats::addGamesPlayed
        );
    }
}
