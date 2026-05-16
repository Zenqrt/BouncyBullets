package dev.zenqrt.bouncybullets.game;

import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.games.GameSettings;
import dev.zenqrt.bouncybullets.game.games.Loadout;
import dev.zenqrt.bouncybullets.game.games.kit.PlayerClasses;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class GameManager {

    private final Map<Integer, BouncyBulletGame> games = new HashMap<>();
    private final Map<UUID, Integer> playerGames = new HashMap<>();
    private final AtomicInteger nextGameId;

    public GameManager() {
        this.nextGameId = new AtomicInteger(0);
    }

    public BouncyBulletGame createGame(GameSettings settings) {
        int gameId = this.nextGameId.incrementAndGet();

        BouncyBulletGame game = new BouncyBulletGame(gameId, settings);
        this.games.put(gameId, game);

        return game;
    }

    public void deleteGame(int gameId) {
        this.games.remove(gameId);
    }

    public Optional<BouncyBulletGame> findGame(int gameId) {
        BouncyBulletGame game = this.games.get(gameId);

        return game == null ? Optional.empty() : Optional.of(game);
    }

    public void joinGame(Player player, BouncyBulletGame game) {
        game.insertPlayer(player, new Loadout(PlayerClasses.STEALTH.getPlayerClass()));
        this.playerGames.put(player.getUniqueId(), game.getId());
    }

    public void leaveGame(Player player, BouncyBulletGame game) {
        game.removePlayer(player.getUniqueId());
        this.playerGames.remove(player.getUniqueId(), game.getId());
    }

    public boolean isInGame(UUID uuid) {
        return this.playerGames.containsKey(uuid);
    }
}
