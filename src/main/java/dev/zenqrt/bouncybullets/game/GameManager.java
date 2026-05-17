package dev.zenqrt.bouncybullets.game;

import dev.zenqrt.bouncybullets.BouncyBulletsPlugin;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.games.GameSettings;
import dev.zenqrt.bouncybullets.loadout.Loadout;
import dev.zenqrt.bouncybullets.loadout.kit.PlayerClasses;
import dev.zenqrt.bouncybullets.lobby.LobbyManager;
import dev.zenqrt.bouncybullets.map.FreeForAllActiveGameMap;
import dev.zenqrt.bouncybullets.map.GameMap;
import dev.zenqrt.bouncybullets.map.GameMapManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class GameManager {

    private final Map<Integer, BouncyBulletGame> games = new HashMap<>();
    private final Map<UUID, BouncyBulletGame> playerGames = new HashMap<>();
    private final LobbyManager lobbyManager;
    private final GameMapManager mapManager;
    private final BouncyBulletsPlugin plugin;
    private final AtomicInteger nextGameId;

    public GameManager(BouncyBulletsPlugin plugin, GameMapManager mapManager, LobbyManager lobbyManager) {
        this.nextGameId = new AtomicInteger(0);
        this.plugin = plugin;
        this.mapManager = mapManager;
        this.lobbyManager = lobbyManager;
    }

    public BouncyBulletGame createGame(GameSettings settings, GameMap map) {
        int gameId = this.nextGameId.incrementAndGet();

        World gameWorld = this.mapManager.createGameWorld(gameId, map);
        FreeForAllActiveGameMap activeMap = new FreeForAllActiveGameMap(gameWorld, map.configuration());

        BouncyBulletGame game = new BouncyBulletGame(gameId, this.plugin, settings, activeMap, this, this.lobbyManager);
        this.games.put(gameId, game);

        return game;
    }

    public void deleteGame(BouncyBulletGame game) {
        this.games.remove(game.getId());
        tryDeleteGameWorld(game);
    }

    private void tryDeleteGameWorld(BouncyBulletGame game) {
        Bukkit.getScheduler().runTaskTimer(this.plugin, task -> {
            try {
                this.mapManager.deleteGameWorld(game.getId(), game.getGameMap().world());
                task.cancel();
            } catch (RuntimeException ex) {
                this.plugin.getSLF4JLogger().error("Failed to delete game world '{}': {}\nRetrying...", game.getGameMap().world().getName(), ex.getMessage());
            }
        }, 20, 40);
    }

    public Optional<BouncyBulletGame> findGame(int gameId) {
        BouncyBulletGame game = this.games.get(gameId);

        return game == null ? Optional.empty() : Optional.of(game);
    }

    public void joinGame(Player player, BouncyBulletGame game) {
        game.insertPlayer(player, new Loadout(PlayerClasses.STEALTH.getPlayerClass()));
        this.playerGames.put(player.getUniqueId(), game);
    }

    public void leaveGame(Player player, BouncyBulletGame game) {
        game.removePlayer(player.getUniqueId());
        this.playerGames.remove(player.getUniqueId(), game);
    }

    public Optional<BouncyBulletGame> findPlayerGame(UUID uuid) {
        BouncyBulletGame game = this.playerGames.get(uuid);

        return game == null ? Optional.empty() : Optional.of(game);
    }

    public boolean isInGame(UUID uuid) {
        return this.playerGames.containsKey(uuid);
    }
}
