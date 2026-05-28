package dev.zenqrt.bouncybullets.stats.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.zenqrt.bouncybullets.stats.PlayerStats;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class JSONPlayerStatsRepository implements PlayerStatsRepository {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private final Path directoryPath;

    public JSONPlayerStatsRepository(Path directoryPath) {
        this.directoryPath = directoryPath;
    }

    @Override
    public CompletableFuture<Void> initialize() {
        return CompletableFuture.runAsync(() -> {
            try {
                Files.createDirectories(this.directoryPath);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Override
    public CompletableFuture<Optional<PlayerStats>> load(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            Path path = this.directoryPath.resolve(uuid + ".json");

            if (Files.notExists(path))
                return Optional.empty();

            try (Reader reader = Files.newBufferedReader(path)) {
                return Optional.of(
                        GSON.fromJson(reader, PlayerStats.class)
                );
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Override
    public CompletableFuture<Void> save(UUID uuid, PlayerStats stats) {
        return CompletableFuture.runAsync(() -> {
            Path path = this.directoryPath.resolve(uuid + ".json");

            try {
                Files.createDirectories(path.getParent());

                try (Writer writer = Files.newBufferedWriter(path)) {
                    GSON.toJson(stats, writer);
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Override
    public CompletableFuture<Void> delete(UUID uuid) {
        return CompletableFuture.runAsync(() -> {
            Path path = this.directoryPath.resolve(uuid + ".json");

            if (Files.notExists(path))
                return;

            try {
                Files.delete(path);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }
}
