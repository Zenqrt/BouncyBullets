package dev.zenqrt.bouncybullets.config;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

public final class ServerConfig {

    private Location lobbySpawn;

    private final Plugin plugin;

    public ServerConfig(Plugin plugin) {
        this.plugin = plugin;
        cacheValues();
    }

    public void save() {
        this.plugin.saveConfig();
    }

    public void reload() {
        cacheValues();
    }

    private void cacheValues() {
        this.lobbySpawn = this.plugin.getConfig().getLocation("Lobby.SpawnLocation", null);
    }

    public void setLobbySpawn(Location location) {
        this.lobbySpawn = location;

        this.plugin.getConfig().set("Lobby.SpawnLocation", location);
    }

    public Location getLobbySpawn() {
        return lobbySpawn;
    }
}
