package dev.zenqrt.bouncybullets.command.commands;

import dev.zenqrt.bouncybullets.config.ServerConfig;
import dev.zenqrt.bouncybullets.game.GameManager;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.games.GameSettings;
import dev.zenqrt.bouncybullets.map.GameMap;
import dev.zenqrt.bouncybullets.map.GameMapManager;
import dev.zenqrt.bouncybullets.utils.Messages;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Named;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.exception.CommandErrorException;

@Command({"bouncybullets", "bb"})
public final class BouncyBulletsCommand {

    private final ServerConfig config;
    private final GameMapManager mapManager;
    private final GameManager gameManager;

    public BouncyBulletsCommand(GameManager gameManager, GameMapManager mapManager, ServerConfig config) {
        this.gameManager = gameManager;
        this.mapManager = mapManager;
        this.config = config;
    }

    @Subcommand("setlobby")
    public void onSetLobby(
            Player executor
    ) {
        Location location = executor.getLocation();

        this.config.setLobbySpawn(location);
        this.config.save();

        Messages.sendCommandSuccess(executor, "Set lobby spawn to %.2f, %.2f, %.2f".formatted(location.getX(), location.getY(), location.getZ()));
    }

    @Subcommand("lobby")
    public void onLobby(
            Player executor
    ) {
        executor.teleport(this.config.getLobbySpawn());
    }

    @Subcommand("game create")
    public void onGameCreate(
            Player executor,
            @Named("map") String mapId,
            @Named("min_players") int minPlayers,
            @Named("max_players") int maxPlayers,
            @Named("game_time") int gameTime,
            @Named("join_on_create") boolean joinOnCreate) {
        Messages.sendCommandInfo(executor, "Creating game...");

        GameSettings settings = new GameSettings(minPlayers, maxPlayers, gameTime);
        GameMap map = this.mapManager.findGameMap(mapId)
                .orElseThrow(() -> new CommandErrorException("Could not find map '" + mapId + "'"));

        BouncyBulletGame game = this.gameManager.createGame(settings, map);
        game.start();

        Messages.sendCommandSuccess(executor, "Created game with id " + game.getId());

        if (!joinOnCreate)
            return;

        Messages.sendCommandInfo(executor, "Joining game...");
        this.gameManager.joinGame(executor, game);
    }

    @Subcommand("game join")
    public void onGameJoin(
            Player executor,
            @Named("game_id") int gameId
    ) {
        if (this.gameManager.isInGame(executor.getUniqueId()))
            throw new CommandErrorException("You are already in a game!");

        Messages.sendCommandInfo(executor, "Finding game...");

        BouncyBulletGame game = this.gameManager.findGame(gameId)
                .orElseThrow(() -> new CommandErrorException("Could not find game with id " + gameId));

        Messages.sendCommandInfo(executor, "Joining game...");
        this.gameManager.joinGame(executor, game);
    }
}
