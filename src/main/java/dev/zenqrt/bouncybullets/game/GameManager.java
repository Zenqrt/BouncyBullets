package dev.zenqrt.bouncybullets.game;

import dev.zenqrt.bouncybullets.BouncyBulletsPlugin;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.games.GameSettings;
import dev.zenqrt.bouncybullets.map.FreeForAllActiveGameMap;
import dev.zenqrt.bouncybullets.map.GameMap;
import dev.zenqrt.bouncybullets.map.GameMapManager;
import dev.zenqrt.bouncybullets.player.PlayerSessionManager;
import dev.zenqrt.bouncybullets.stats.PlayerStatsManager;
import org.bukkit.Bukkit;
import org.bukkit.GameRules;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public final class GameManager {

    private final Map<Integer, BouncyBulletGame> games = new HashMap<>();
    private final GameMapManager mapManager;
    private final BouncyBulletsPlugin plugin;
    private final AtomicInteger nextGameId;

    public GameManager(BouncyBulletsPlugin plugin, GameMapManager mapManager) {
        this.nextGameId = new AtomicInteger(0);
        this.plugin = plugin;
        this.mapManager = mapManager;
    }

    public BouncyBulletGame createGame(GameSettings settings, GameMap map, PlayerSessionManager sessionManager, PlayerStatsManager statsManager) {
        int gameId = this.nextGameId.incrementAndGet();

        World gameWorld = this.mapManager.createGameWorld(gameId, map);
        gameWorld.setGameRule(GameRules.LOCATOR_BAR, false);

        FreeForAllActiveGameMap activeMap = new FreeForAllActiveGameMap(gameWorld, map.configuration());

        BouncyBulletGame game = new BouncyBulletGame(gameId, this.plugin, settings, activeMap, this, sessionManager, statsManager);
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
}
