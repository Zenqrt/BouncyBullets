package dev.zenqrt.bouncybullets.game.games;

import dev.zenqrt.bouncybullets.event.events.PlayerJoinGameEvent;
import dev.zenqrt.bouncybullets.event.events.PlayerQuitGameEvent;
import dev.zenqrt.bouncybullets.game.base.Game;
import dev.zenqrt.bouncybullets.game.base.GameState;
import dev.zenqrt.bouncybullets.game.games.states.PregameGameState;
import dev.zenqrt.bouncybullets.player.GamePlayerList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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

    public void insertPlayer(Player player, Loadout loadout) {
        BouncyBulletGamePlayer gamePlayer = new BouncyBulletGamePlayer(
                player.getUniqueId(),
                player,
                0,
                0,
                loadout
        );

        this.players.put(player.getUniqueId(), gamePlayer);

        Bukkit.getPluginManager().callEvent(new PlayerJoinGameEvent(player, this));
    }

    public void removePlayer(UUID uuid) {
        BouncyBulletGamePlayer player = players.remove(uuid);

        Bukkit.getPluginManager().callEvent(new PlayerQuitGameEvent(player.player(), this));
    }

    public boolean hasPlayer(UUID uuid) {
        return this.players.containsKey(uuid);
    }

    public boolean canJoinGame() {
        return true;
//        return this.gameState instanceof PregameGameState && players.size() < gameSettings.maxPlayers();
    }

    public GameSettings getGameSettings() {
        return gameSettings;
    }
}
