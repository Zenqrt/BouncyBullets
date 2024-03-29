package dev.zenqrt.bouncybullets;

import dev.zenqrt.bouncybullets.events.PlayerJoinListeners;
import dev.zenqrt.bouncybullets.events.PlayerListeners;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.map.GameMapRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Objects;
import java.util.function.BiConsumer;

public final class BouncyBullets extends JavaPlugin {

    private static BouncyBulletGame game;
    private static BouncyBullets instance;


    @Override
    public void onEnable() {
        instance = this;
        GameMapRegistry.registerGameMaps(new File(getDataFolder(), "maps"));
        Bukkit.getWorlds().forEach(world -> world.setAutoSave(false));

        game = new BouncyBulletGame(1);

        game.start();

        Bukkit.getPluginManager().registerEvents(new PlayerJoinListeners(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListeners(), this);

        registerCommand("tpgameworld", (player, args) -> {
            player.sendMessage(Component.text("Teleporting...", NamedTextColor.GRAY).decorate(TextDecoration.ITALIC));
            player.teleport(Bukkit.getWorld("game_world_" + game.getId()).getSpawnLocation());
        });

        registerCommand("freeze", (player, args) -> {
            boolean canMoveOn = !game.getGameState().canMoveOn();

            game.getGameState().setCanMoveOn(canMoveOn);

            if (canMoveOn) {
                Bukkit.broadcast(Component.text(player.getName() + " unfroze the current game state!"));
            } else {
                Bukkit.broadcast(Component.text(player.getName() + " froze the current game state!"));
            }
        });

        registerCommand("backup", (player, args) -> {
            Bukkit.broadcast(Component.text("Saving backup of the world...").decorate(TextDecoration.ITALIC));
            try {
                File newDirectory = new File(getDataFolder(), "backups/world-" + Instant.now().toString().replace(":", "-"));
                FileUtils.copyDirectory(player.getWorld().getWorldFolder(), newDirectory, file -> !file.getName().equals("session.lock"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Bukkit.broadcast(Component.text("Successfully saved a backup of the world.", NamedTextColor.GREEN));
        });
    }

    @Override
    public void onDisable() {
    }



    public static BouncyBulletGame getGame() {
        return game;
    }

    public static BouncyBullets getInstance() {
        return instance;
    }

    private void registerCommand(String name, BiConsumer<Player, String[]> commandHandler) {
        Objects.requireNonNull(getCommand(name)).setExecutor((sender, cmd, label, args) -> {
            commandHandler.accept((Player) sender, args);
            return true;
        });
    }

}
