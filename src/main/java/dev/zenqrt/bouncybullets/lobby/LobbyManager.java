package dev.zenqrt.bouncybullets.lobby;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class LobbyManager {

    private final Location lobbySpawn;

    public LobbyManager(Location lobbySpawn) {
        this.lobbySpawn = lobbySpawn;
    }

    public void sendToLobby(Player player) {
        setupLobbyPlayer(player);

        player.teleport(lobbySpawn);
    }

    public void setupLobbyPlayer(Player player) {
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.clearActivePotionEffects();
    }

}
