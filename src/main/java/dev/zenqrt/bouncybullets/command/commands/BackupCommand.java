package dev.zenqrt.bouncybullets.command.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import revxrsal.commands.annotation.Command;

import java.io.File;
import java.io.IOException;
import java.time.Instant;

public final class BackupCommand {

    private final Plugin plugin;

    public BackupCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Command("backup")
    public void onBackup(
            Player player
    ) {
        Bukkit.broadcast(Component.text("Saving backup of the world...").decorate(TextDecoration.ITALIC));

        World world = player.getWorld();
        world.save();

        try {
            File newDirectory = new File(this.plugin.getDataFolder(), "backups/world-" + Instant.now().toString().replace(":", "-"));
            FileUtils.copyDirectory(world.getWorldFolder(), newDirectory, file -> !file.getName().equals("session.lock"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Bukkit.broadcast(Component.text("Successfully saved a backup of the world.", NamedTextColor.GREEN));
    }

}
