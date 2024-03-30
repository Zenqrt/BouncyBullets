package dev.zenqrt.bouncybullets.game.games;

import dev.zenqrt.bouncybullets.event.PlayerJoinGameEvent;
import dev.zenqrt.bouncybullets.event.PlayerQuitGameEvent;
import dev.zenqrt.bouncybullets.game.base.Game;
import dev.zenqrt.bouncybullets.game.base.GameState;
import dev.zenqrt.bouncybullets.game.games.states.PregameGameState;
import dev.zenqrt.bouncybullets.player.GamePlayerList;
import org.bukkit.Bukkit;

import java.util.UUID;

public final class BouncyBulletGame extends Game {

    private final GameSettings gameSettings;
    private final GamePlayerList players = new GamePlayerList();

    public BouncyBulletGame(int id, GameSettings gameSettings) {
        super(id, null);

        this.gameSettings = gameSettings;
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
        return this.gameState instanceof PregameGameState && players.size() < gameSettings.maxPlayers();
    }

    public GameSettings getGameSettings() {
        return gameSettings;
    }
}
