package dev.zenqrt.bouncybullets.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public final class TaskManager {

    private final List<BukkitTask> tasks = new ArrayList<>();
    private final Plugin plugin;

    public TaskManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void runTaskTimer(Runnable runnable, long delayTicks, long periodTicks) {
        registerTask(
                Bukkit.getScheduler().runTaskTimer(this.plugin, runnable, delayTicks, periodTicks)
        );
    }

    public void runTaskTimer(BukkitRunnable runnable, long delayTicks, long periodTicks) {
        registerTask(
                runnable.runTaskTimer(this.plugin, delayTicks, periodTicks)
        );
    }

    private void registerTask(BukkitTask task) {
        this.tasks.add(task);
    }

    public void removeAllTasks() {
        tasks.forEach(BukkitTask::cancel);
        tasks.clear();
    }

}
