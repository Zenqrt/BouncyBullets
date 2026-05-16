package dev.zenqrt.bouncybullets.lobby;

import dev.zenqrt.bouncybullets.config.ServerConfig;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public final class LobbyManager {

    private final ServerConfig config;

    // NOTE: Consider making a separate config for lobby stuff and store it in here
    //       to prevent have to use ServerConfig in a weird place (here).
    public LobbyManager(ServerConfig config) {
        this.config = config;
    }

    public void sendToLobby(Player player) {
        setupLobbyPlayer(player);

        player.teleport(this.config.getLobbySpawn());
    }

    public void setupLobbyPlayer(Player player) {
        player.getInventory().clear();

        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.clearActivePotionEffects();
    }

}
