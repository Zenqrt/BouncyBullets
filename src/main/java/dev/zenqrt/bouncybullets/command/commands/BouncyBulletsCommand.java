package dev.zenqrt.bouncybullets.command.commands;

import dev.zenqrt.bouncybullets.game.GameManager;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.games.GameSettings;
import dev.zenqrt.bouncybullets.utils.Messages;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Named;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.exception.CommandErrorException;

@Command({"bouncybullets", "bb"})
public final class BouncyBulletsCommand {

    private final GameManager gameManager;

    public BouncyBulletsCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Subcommand("game create")
    public void onGameCreate(
            Player executor,
            @Named("min_players") int minPlayers,
            @Named("max_players") int maxPlayers,
            @Named("game_time") int gameTime,
            @Named("join_on_create") boolean joinOnCreate) {
        Messages.sendCommandInfo(executor, "Creating game...");

        GameSettings settings = new GameSettings(minPlayers, maxPlayers, gameTime);
        BouncyBulletGame game = this.gameManager.createGame(settings);
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
