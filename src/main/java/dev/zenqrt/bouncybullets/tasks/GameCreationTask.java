package dev.zenqrt.bouncybullets.tasks;

import dev.zenqrt.bouncybullets.game.GameManager;
import dev.zenqrt.bouncybullets.game.games.GameSettings;
import dev.zenqrt.bouncybullets.map.GameMap;
import dev.zenqrt.bouncybullets.map.GameMapManager;
import dev.zenqrt.bouncybullets.player.PlayerSessionManager;
import dev.zenqrt.bouncybullets.stats.PlayerStatsManager;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public final class GameCreationTask implements Runnable {

    private final int maintainAvailableCount;
    private final GameSettings defaultGameSettings;
    private final PlayerStatsManager statsManager;
    private final PlayerSessionManager sessionManager;
    private final GameMapManager mapManager;
    private final GameManager gameManager;

    public GameCreationTask(GameManager gameManager, GameMapManager mapManager, PlayerSessionManager sessionManager, PlayerStatsManager statsManager, GameSettings defaultGameSettings, int maintainAvailableCount) {
        this.gameManager = gameManager;
        this.mapManager = mapManager;
        this.sessionManager = sessionManager;
        this.statsManager = statsManager;
        this.defaultGameSettings = defaultGameSettings;
        this.maintainAvailableCount = maintainAvailableCount;
    }

    @Override
    public void run() {
        int availableGames = this.gameManager.getAvailableGames().size();

        if (availableGames >= this.maintainAvailableCount)
            return;

        int gamesToCreate = this.maintainAvailableCount - availableGames;

        for (int i = 0; i < gamesToCreate; i++) {
            GameMap randomMap = findRandomMap(ThreadLocalRandom.current());

            this.gameManager.createGame(
                    this.defaultGameSettings,
                    randomMap,
                    this.sessionManager,
                    this.statsManager
            ).start();
        }
    }

    private GameMap findRandomMap(Random random) {
        String mapId = this.mapManager.getGameMapIds()
                .get(random.nextInt(this.mapManager.getGameMapIds().size()));

        return this.mapManager.findGameMap(mapId)
                .orElseThrow();
    }
}
