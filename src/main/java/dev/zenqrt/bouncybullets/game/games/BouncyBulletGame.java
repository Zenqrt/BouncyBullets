package dev.zenqrt.bouncybullets.game.games;

import dev.zenqrt.bouncybullets.BouncyBulletsPlugin;
import dev.zenqrt.bouncybullets.event.events.PlayerJoinGameEvent;
import dev.zenqrt.bouncybullets.event.events.PlayerQuitGameEvent;
import dev.zenqrt.bouncybullets.game.GameManager;
import dev.zenqrt.bouncybullets.game.base.Game;
import dev.zenqrt.bouncybullets.game.games.states.AnnounceWinnerGameState;
import dev.zenqrt.bouncybullets.game.games.states.BattleGameState;
import dev.zenqrt.bouncybullets.game.games.states.PregameGameState;
import dev.zenqrt.bouncybullets.game.games.states.SendPlayersToLobbyGameState;
import dev.zenqrt.bouncybullets.lobby.LobbyManager;
import dev.zenqrt.bouncybullets.map.FreeForAllActiveGameMap;
import dev.zenqrt.bouncybullets.player.GamePlayerList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public final class BouncyBulletGame extends Game {

    private final GamePlayerList players;
    private final GameManager gameManager;
    private final FreeForAllActiveGameMap gameMap;
    private final GameSettings gameSettings;
    private final BouncyBulletsPlugin plugin;

    public BouncyBulletGame(int id, BouncyBulletsPlugin plugin, GameSettings gameSettings, FreeForAllActiveGameMap gameMap, GameManager gameManager, LobbyManager lobbyManager) {
        super(id);

        this.plugin = plugin;
        this.gameSettings = gameSettings;
        this.gameMap = gameMap;
        this.gameManager = gameManager;
        this.players = new GamePlayerList();

        this.states = List.of(
                new PregameGameState(this, this.players),
                new BattleGameState(this, this.players, gameMap),
                new AnnounceWinnerGameState(this, this.players, 200),  // 10 seconds
                new SendPlayersToLobbyGameState(this, gameManager, lobbyManager)
        );
    }

    @Override
    protected void onStateEnd() {
        super.onStateEnd();

        this.players.values().stream()
                .map(BouncyBulletGamePlayer::player)
                .filter(player -> player.getWorld() == this.gameMap.world())
                .forEach(player -> player.kick(Component.text("The game you were in shut down!", NamedTextColor.RED)));
        this.players.clear();

        this.gameManager.deleteGame(this);
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

    public GamePlayerList getPlayers() {
        return players;
    }

    public boolean hasPlayer(UUID uuid) {
        return this.players.containsKey(uuid);
    }

    @Override
    public boolean canPlayersJoin() {
        return getGameState().canPlayersJoin() && players.size() < gameSettings.maxPlayers();
    }

    public FreeForAllActiveGameMap getGameMap() {
        return gameMap;
    }

    public GameSettings getGameSettings() {
        return gameSettings;
    }

    public BouncyBulletsPlugin getPlugin() {
        return plugin;
    }
}
