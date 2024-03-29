package dev.zenqrt.bouncybullets.game.games;

import dev.zenqrt.bouncybullets.event.PlayerJoinGameEvent;
import dev.zenqrt.bouncybullets.event.PlayerQuitGameEvent;
import dev.zenqrt.bouncybullets.game.Game;
import dev.zenqrt.bouncybullets.game.GameState;
import dev.zenqrt.bouncybullets.game.games.states.PregameGameState;
import dev.zenqrt.bouncybullets.player.GamePlayerList;
import org.bukkit.Bukkit;

import java.util.UUID;

public final class BouncyBulletGame extends Game {

    private final GamePlayerList players = new GamePlayerList();

    public BouncyBulletGame(int id) {
        super(id, null);

        this.gameState = createStartingGameState(this, players);
    }

    private static GameState createStartingGameState(BouncyBulletGame game, GamePlayerList players) {
        return new PregameGameState(game, players);
    }

    public void insertPlayer(BouncyBulletPlayer player) {
        players.put(player.uuid(), player);

        Bukkit.getPluginManager().callEvent(new PlayerJoinGameEvent(player.player(), this));
    }

    public void removePlayer(UUID uuid) {
        BouncyBulletPlayer player = players.remove(uuid);

        Bukkit.getPluginManager().callEvent(new PlayerQuitGameEvent(player.player(), this));
    }

    public boolean canJoinGame() {
        return true;
    }
}
