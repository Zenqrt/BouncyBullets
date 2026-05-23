package dev.zenqrt.bouncybullets;

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
import dev.zenqrt.bouncybullets.utils.PlayerUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

import java.io.File;
import java.io.IOException;
import java.time.Instant;

public final class BouncyBulletsPlugin extends JavaPlugin {

    private static GameMapManager mapManager;
    private static BouncyBulletsPlugin instance;

    @Override
    public void onEnable() {
        instance = this;

        File serverConfigFile = new File(getDataFolder(), "config.yml");

        if (!serverConfigFile.exists()) {
            try {
                serverConfigFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        ServerConfig serverConfig = new ServerConfig(serverConfigFile);

        mapManager = new GameMapManager(this, new File(getDataFolder(), "maps"));
        mapManager.loadGameMaps();

        LobbyManager lobbyManager = new LobbyManager(serverConfig);
        GameManager gameManager = new GameManager(this, mapManager, lobbyManager);

        Lamp<BukkitCommandActor> lamp = BukkitLamp.builder(this).build();
        lamp.register(
                new BouncyBulletsCommand(gameManager, mapManager, serverConfig)
        );

        Bukkit.getPluginManager().registerEvents(new PlayerJoinListeners(gameManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListeners(), this);
        Bukkit.getPluginManager().registerEvents(new GameItemListeners(gameManager), this);
        Bukkit.getPluginManager().registerEvents(new GunListeners(gameManager), this);
        Bukkit.getPluginManager().registerEvents(GameItems.SNIPER_ACTIVE_ABILITY, this);
        Bukkit.getPluginManager().registerEvents(GameItems.HEAVY_ACTIVE_ABILITY, this);
        registerCommand("backup", (source, _) -> {
            Bukkit.broadcast(Component.text("Saving backup of the world...").decorate(TextDecoration.ITALIC));

            World world = source.getLocation().getWorld();
            world.save();

            try {
                File newDirectory = new File(getDataFolder(), "backups/world-" + Instant.now().toString().replace(":", "-"));
                FileUtils.copyDirectory(world.getWorldFolder(), newDirectory, file -> !file.getName().equals("session.lock"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Bukkit.broadcast(Component.text("Successfully saved a backup of the world.", NamedTextColor.GREEN));
        });
    }

    @Override
    public void onDisable() {
        Bukkit.getOnlinePlayers().forEach(PlayerUtils::forceRemove);
        mapManager.deleteAllGameWorlds();
    }

    public static BouncyBulletsPlugin getInstance() {
        return instance;
    }

    public static NamespacedKey createKey(String id) {
        return NamespacedKey.fromString(id, instance);
    }

}
