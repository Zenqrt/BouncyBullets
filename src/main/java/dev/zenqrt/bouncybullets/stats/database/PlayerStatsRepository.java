package dev.zenqrt.bouncybullets.stats.database;

import dev.zenqrt.bouncybullets.stats.PlayerStats;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerStatsRepository {

    CompletableFuture<Void> initialize();

    CompletableFuture<Optional<PlayerStats>> load(UUID uuid);
    CompletableFuture<Void> save(UUID uuid, PlayerStats stats);
    CompletableFuture<Void> delete(UUID uuid);

}
