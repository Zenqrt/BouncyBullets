package dev.zenqrt.bouncybullets;

import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import org.bukkit.plugin.java.JavaPlugin;

public final class BouncyBullets extends JavaPlugin {

    private final BouncyBulletGame game = new BouncyBulletGame(1);

    @Override
    public void onEnable() {
        // Plugin startup logic

        game.start();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
