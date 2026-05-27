package dev.zenqrt.bouncybullets;

import dev.zenqrt.bouncybullets.command.commands.BackupCommand;
import dev.zenqrt.bouncybullets.command.commands.BouncyBulletsCommand;
import dev.zenqrt.bouncybullets.config.ServerConfig;
import dev.zenqrt.bouncybullets.event.listeners.GameItemListeners;
import dev.zenqrt.bouncybullets.event.listeners.GunListeners;
import dev.zenqrt.bouncybullets.event.listeners.PlayerJoinListeners;
import dev.zenqrt.bouncybullets.event.listeners.PlayerListeners;
import dev.zenqrt.bouncybullets.game.GameManager;
import dev.zenqrt.bouncybullets.item.GameItems;
import dev.zenqrt.bouncybullets.lobby.LobbyManager;
import dev.zenqrt.bouncybullets.map.GameMapManager;
import dev.zenqrt.bouncybullets.stats.PlayerStatsManager;
import dev.zenqrt.bouncybullets.stats.database.JSONPlayerStatsRepository;
import dev.zenqrt.bouncybullets.stats.database.PlayerStatsRepository;
import dev.zenqrt.bouncybullets.tasks.AutoRepositorySaveTask;
import dev.zenqrt.bouncybullets.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

import java.io.File;
import java.io.IOException;

public final class BouncyBulletsPlugin extends JavaPlugin {

    private static final int AUTO_REPOSITORY_SAVE_INTERVAL_TICKS = 6000;        // 5 minutes

    private GameMapManager mapManager;
    private PlayerStatsManager statsManager;
    private static BouncyBulletsPlugin instance;

    @Override
    public void onEnable() {
        instance = this;

        ServerConfig serverConfig = createOrGetConfig();

        PlayerStatsRepository repository = new JSONPlayerStatsRepository(getDataPath().resolve("stats"));
        repository.initialize();

        this.statsManager = new PlayerStatsManager(this, repository);

        this.mapManager = new GameMapManager(this, new File(getDataFolder(), "maps"));
        this.mapManager.loadGameMaps();

        LobbyManager lobbyManager = new LobbyManager(serverConfig);
        GameManager gameManager = new GameManager(this, this.mapManager, lobbyManager, this.statsManager);

        Lamp<BukkitCommandActor> lamp = BukkitLamp.builder(this).build();
        lamp.register(
                new BouncyBulletsCommand(gameManager, this.mapManager, serverConfig, lobbyManager),
                new BackupCommand(this)
        );

        Bukkit.getPluginManager().registerEvents(new PlayerJoinListeners(this.statsManager, gameManager, lobbyManager, serverConfig), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListeners(), this);
        Bukkit.getPluginManager().registerEvents(new GameItemListeners(gameManager), this);
        Bukkit.getPluginManager().registerEvents(new GunListeners(gameManager), this);
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
        File serverConfigFile = new File(getDataFolder(), "config.yml");

        if (!serverConfigFile.exists()) {
            try {
                serverConfigFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return new ServerConfig(serverConfigFile);
    }

    @Override
    public void onDisable() {
        this.statsManager.saveDirty();

        Bukkit.getOnlinePlayers().forEach(PlayerUtils::forceRemove);
        this.mapManager.deleteAllGameWorlds();
    }

    public static BouncyBulletsPlugin getInstance() {
        return instance;
    }

    public static NamespacedKey createKey(String id) {
        return NamespacedKey.fromString(id, instance);
    }

}
