package dev.zenqrt.bouncybullets;

import dev.zenqrt.bouncybullets.events.PlayerJoinListeners;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class BouncyBullets extends JavaPlugin {

    private static BouncyBulletGame game;
    private static BouncyBullets instance;

    @Override
    public void onEnable() {
        instance = this;
        // Plugin startup logic
        game = new BouncyBulletGame(1);

        game.start();

        Bukkit.getPluginManager().registerEvents(new PlayerJoinListeners(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static BouncyBulletGame getGame() {
        return game;
    }

    public static BouncyBullets getInstance() {
        return instance;
    }
}
