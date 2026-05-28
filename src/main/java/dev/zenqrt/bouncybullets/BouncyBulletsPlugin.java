package dev.zenqrt.bouncybullets;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dev.zenqrt.bouncybullets.command.commands.BackupCommand;
import dev.zenqrt.bouncybullets.command.commands.BouncyBulletsCommand;
import dev.zenqrt.bouncybullets.config.ServerConfig;
import dev.zenqrt.bouncybullets.event.listeners.GameItemListeners;
import dev.zenqrt.bouncybullets.event.listeners.GunListeners;
import dev.zenqrt.bouncybullets.event.listeners.PlayerJoinListeners;
import dev.zenqrt.bouncybullets.event.listeners.PlayerListeners;
import dev.zenqrt.bouncybullets.game.GameManager;
import dev.zenqrt.bouncybullets.item.GameItems;
import dev.zenqrt.bouncybullets.lobby.LobbyInstance;
import dev.zenqrt.bouncybullets.map.GameMapManager;
import dev.zenqrt.bouncybullets.player.PlayerSessionManager;
import dev.zenqrt.bouncybullets.stats.PlayerStatsManager;
import dev.zenqrt.bouncybullets.stats.database.JSONPlayerStatsRepository;
import dev.zenqrt.bouncybullets.stats.database.MongoPlayerStatsRepository;
import dev.zenqrt.bouncybullets.stats.database.PlayerStatsRepository;
import dev.zenqrt.bouncybullets.tasks.AutoRepositorySaveTask;
import dev.zenqrt.bouncybullets.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public final class BouncyBulletsPlugin extends JavaPlugin {

    private static final int AUTO_REPOSITORY_SAVE_INTERVAL_TICKS = 6000;        // 5 minutes

    private @Nullable MongoClient mongoClient;
    private GameMapManager mapManager;
    private PlayerStatsManager statsManager;
    private static BouncyBulletsPlugin instance;

    @Override
    public void onEnable() {
        instance = this;

        ServerConfig serverConfig = createOrGetConfig();

        PlayerStatsRepository repository = getRepositoryFromConfig(super.getConfig());

        try {
            repository.initialize().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to initialize player stats repository", e);
        }

        this.statsManager = new PlayerStatsManager(this, repository);

        this.mapManager = new GameMapManager(this, new File(getDataFolder(), "maps"));
        this.mapManager.loadGameMaps();

        LobbyInstance lobby = new LobbyInstance(serverConfig.getLobbySpawn());

        GameManager gameManager = new GameManager(this, this.mapManager);
        PlayerSessionManager sessionManager = new PlayerSessionManager(lobby, this.statsManager);

        Lamp<BukkitCommandActor> lamp = BukkitLamp.builder(this).build();
        lamp.register(
                new BouncyBulletsCommand(gameManager, this.mapManager, serverConfig, sessionManager, this.statsManager),
                new BackupCommand(this)
        );

        Bukkit.getPluginManager().registerEvents(new PlayerJoinListeners(lobby, this.statsManager, sessionManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListeners(), this);
        Bukkit.getPluginManager().registerEvents(new GameItemListeners(sessionManager), this);
        Bukkit.getPluginManager().registerEvents(new GunListeners(sessionManager), this);
        Bukkit.getPluginManager().registerEvents(GameItems.SNIPER_ACTIVE_ABILITY, this);
        Bukkit.getPluginManager().registerEvents(GameItems.HEAVY_ACTIVE_ABILITY, this);

        Bukkit.getScheduler().runTaskTimer(
                this,
                new AutoRepositorySaveTask(this, this.statsManager),
                0,
                AUTO_REPOSITORY_SAVE_INTERVAL_TICKS
        );
    }

    private ServerConfig createOrGetConfig() {
        super.saveDefaultConfig();

        return new ServerConfig(this);
    }

    private PlayerStatsRepository getRepositoryFromConfig(FileConfiguration config) {
        if (config.contains("mongodb")) {
            super.getSLF4JLogger().info("MongoDB Database initializing...");

            String connectionString = Objects.requireNonNull(
                    config.getString("mongodb.connection_string"),
                    "Missing database.connection_string from config.yml"
            );

            this.mongoClient = MongoClients.create(connectionString);

            super.getSLF4JLogger().info("MongoDB Database is now active!");

            return MongoPlayerStatsRepository.parse(this.mongoClient, config);
        }

        return new JSONPlayerStatsRepository(
                super.getDataPath().resolve("stats")
        );
    }

    @Override
    public void onDisable() {
        this.statsManager.saveDirty();

        Bukkit.getOnlinePlayers().forEach(PlayerUtils::forceRemove);
        this.mapManager.deleteAllGameWorlds();

        if (this.mongoClient != null)
            this.mongoClient.close();
    }

    public static BouncyBulletsPlugin getInstance() {
        return instance;
    }

    public static NamespacedKey createKey(String id) {
        return NamespacedKey.fromString(id, instance);
    }

}
