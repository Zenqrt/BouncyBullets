package dev.zenqrt.bouncybullets.config;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public final class ServerConfig {

    private Location lobbySpawn;
    private final YamlConfiguration config;
    private final File configFile;

    public ServerConfig(File configFile) {
        this.configFile = configFile;
        this.config = YamlConfiguration.loadConfiguration(configFile);
        cacheValues();
    }

    public void save() {
        try {
            this.config.save(this.configFile);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void reload() {
        cacheValues();
    }

    private void cacheValues() {
        this.lobbySpawn = this.config.getLocation("Lobby.SpawnLocation", null);
    }

    public void setLobbySpawn(Location location) {
        this.lobbySpawn = location;

        this.config.set("Lobby.SpawnLocation", location);
    }

    public Location getLobbySpawn() {
        return lobbySpawn;
    }

}
