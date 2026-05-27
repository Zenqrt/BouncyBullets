package dev.zenqrt.bouncybullets.player;

import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.loadout.Loadout;
import dev.zenqrt.bouncybullets.loadout.kit.PlayerClassType;
import dev.zenqrt.bouncybullets.lobby.LobbyInstance;
import dev.zenqrt.bouncybullets.stats.PlayerStatsManager;
import org.bukkit.entity.Player;

import java.util.*;

public final class PlayerSessionManager {

    private final Map<UUID, BouncyBulletGame> playerToGameMap = new HashMap<>();
    private final Set<UUID> inLobby = new HashSet<>();

    private final PlayerStatsManager statsManager;
    private final LobbyInstance lobby;

    public PlayerSessionManager(LobbyInstance lobby, PlayerStatsManager statsManager) {
        this.lobby = lobby;
        this.statsManager = statsManager;
    }

    public void joinGame(Player player, BouncyBulletGame game) {
        if (isInGame(player.getUniqueId()))
            return;

        tryLeaveLobby(player);

        game.insertPlayer(player, new Loadout(PlayerClassType.STEALTH));
        this.playerToGameMap.put(player.getUniqueId(), game);
    }

    public void joinLobby(Player player, boolean teleport) {
        UUID uuid = player.getUniqueId();

        if (isInLobby(uuid))
            return;

        tryLeaveGame(uuid);

        this.lobby.join(player, this.statsManager.getStatsOrThrow(uuid), teleport);
        this.inLobby.add(uuid);
    }

    public void tryLeaveGame(UUID uuid) {
        BouncyBulletGame game = this.playerToGameMap.remove(uuid);

        if (game != null)
            game.removePlayer(uuid);
    }

    public void tryLeaveLobby(Player player) {
        this.lobby.leave(player);
        this.inLobby.remove(player.getUniqueId());
    }

    public Optional<BouncyBulletGame> findGameSession(UUID uuid) {
        BouncyBulletGame game = this.playerToGameMap.get(uuid);

        return game == null ? Optional.empty() : Optional.of(game);
    }

    public boolean isInGame(UUID uuid) {
        return this.playerToGameMap.containsKey(uuid);
    }

    public boolean isInLobby(UUID uuid) {
        return this.inLobby.contains(uuid);
    }
}
