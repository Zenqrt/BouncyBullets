package dev.zenqrt.bouncybullets.stats.database;

import com.google.gson.Gson;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import dev.zenqrt.bouncybullets.stats.PlayerStats;
import org.bson.Document;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MongoPlayerStatsRepository implements PlayerStatsRepository {

    private static final Gson GSON = new Gson();

    public static MongoPlayerStatsRepository parse(MongoClient client, FileConfiguration config) {
        String databaseName = Objects.requireNonNull(
                config.getString("database.database"),
                "Missing database.database from config.yml"
        );
        String collectionName = Objects.requireNonNull(
                config.getString("database.stats_collection"),
                "Missing database.stats_collection from config.yml"
        );

        MongoCollection<Document> collection = client.getDatabase(databaseName)
                .getCollection(collectionName);

        return new MongoPlayerStatsRepository(collection);
    }

    private final MongoCollection<Document> collection;

    public MongoPlayerStatsRepository(MongoCollection<Document> statsCollection) {
        this.collection = statsCollection;
    }

    @Override
    public CompletableFuture<Void> initialize() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Optional<PlayerStats>> load(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            Document document = this.collection
                    .find(Filters.eq("_id", uuid.toString()))
                    .first();

            if (document == null)
                return Optional.empty();

            PlayerStats stats = GSON.fromJson(
                    document.toJson(),
                    PlayerStats.class
            );

            return Optional.of(stats);
        });
    }

    @Override
    public CompletableFuture<Void> save(UUID uuid, PlayerStats stats) {
        return CompletableFuture.runAsync(() -> {
            Document document = Document.parse(GSON.toJson(stats));
            document.put("_id", uuid.toString());

            this.collection.replaceOne(
                    Filters.eq("_id", uuid.toString()),
                    document,
                    new ReplaceOptions()
                            .upsert(true)
            );
        });
    }

    @Override
    public CompletableFuture<Void> delete(UUID uuid) {
        return CompletableFuture.runAsync(() ->
                this.collection.deleteOne(
                        Filters.eq("_id", uuid.toString())
                )
        );
    }
}
