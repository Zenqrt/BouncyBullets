package dev.zenqrt.bouncybullets.tasks;

import dev.zenqrt.bouncybullets.stats.PlayerStatsManager;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;

public final class AutoRepositorySaveTask implements Runnable {

    private final PlayerStatsManager statsManager;
    private final Logger logger;

    public AutoRepositorySaveTask(Plugin plugin, PlayerStatsManager statsManager) {
        this.logger = plugin.getSLF4JLogger();
        this.statsManager = statsManager;
    }

    @Override
    public void run() {
        this.logger.info("Saving dirty-marked player stats...");

        this.statsManager.saveDirty();

        this.logger.info("Saved player stats!");
    }
}
