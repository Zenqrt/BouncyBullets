package dev.zenqrt.bouncybullets.game.games;

import dev.zenqrt.bouncybullets.event.PlayerJoinGameEvent;
import dev.zenqrt.bouncybullets.game.games.states.WaitingGameState;
import dev.zenqrt.bouncybullets.game.impl.PaperGame;
import dev.zenqrt.bouncybullets.game.impl.PaperGameEventHandler;
import dev.zenqrt.bouncybullets.game.impl.PaperGameState;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class BouncyBulletGame extends PaperGame {

    private final Map<UUID, BouncyBulletPlayer> players = new HashMap<>();

    public BouncyBulletGame(int id) {
        super(id, createStartingGameState(id, new HashMap<>()));
    }

    private static PaperGameState createStartingGameState(int gameId, Map<UUID, BouncyBulletPlayer> players) {
        return new WaitingGameState(new PaperGameEventHandler(), gameId, players);
    }

    public void insertPlayer(BouncyBulletPlayer player) {
        players.put(player.uuid(), player);

        Bukkit.getPluginManager().callEvent(new PlayerJoinGameEvent(player.player(), this));
    }

    public boolean canJoinGame() {
        return true;
    }
}
