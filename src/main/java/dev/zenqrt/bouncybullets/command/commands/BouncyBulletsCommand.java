package dev.zenqrt.bouncybullets.command.commands;

import dev.zenqrt.bouncybullets.config.ServerConfig;
import dev.zenqrt.bouncybullets.game.GameManager;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.games.GameSettings;
import dev.zenqrt.bouncybullets.gui.GameSelectGui;
import dev.zenqrt.bouncybullets.item.GameItem;
import dev.zenqrt.bouncybullets.item.GameItems;
import dev.zenqrt.bouncybullets.map.GameMap;
import dev.zenqrt.bouncybullets.map.GameMapManager;
import dev.zenqrt.bouncybullets.player.PlayerSessionManager;
import dev.zenqrt.bouncybullets.stats.PlayerStats;
import dev.zenqrt.bouncybullets.stats.PlayerStatsManager;
import dev.zenqrt.bouncybullets.utils.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Named;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.exception.CommandErrorException;

import java.util.List;

@Command({"bouncybullets", "bb"})
public final class BouncyBulletsCommand {

    private final PlayerStatsManager statsManager;
    private final PlayerSessionManager sessionManager;
    private final ServerConfig config;
    private final GameMapManager mapManager;
    private final GameManager gameManager;

    public BouncyBulletsCommand(GameManager gameManager, GameMapManager mapManager, ServerConfig config, PlayerSessionManager sessionManager, PlayerStatsManager statsManager) {
        this.gameManager = gameManager;
        this.mapManager = mapManager;
        this.config = config;
        this.sessionManager = sessionManager;
        this.statsManager = statsManager;
    }

    @Subcommand("setlobby")
    @CommandPermission("bouncybullets.command.setlobby")
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
        this.sessionManager.joinLobby(executor, true);
    }

    @Subcommand("give item")
    @CommandPermission("bouncybullets.command.game.give")
    public void onGiveItem(
            Player executor,
            String gameItemId
    ) {
        GameItem gameItem = GameItems.getAllItems().get(gameItemId);

        if (gameItem == null)
            throw new CommandErrorException("Could not find game item with id '" + gameItemId + "'");

        executor.getInventory().addItem(gameItem.buildItemStack());
        Messages.sendCommandSuccess(executor, "Gave item!");
    }

    @Subcommand("game create")
    @CommandPermission("bouncybullets.command.game.create")
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

        BouncyBulletGame game = this.gameManager.createGame(settings, map, this.sessionManager, this.statsManager);
        game.start();

        Messages.sendCommandSuccess(executor, "Created game with id " + game.getId());

        if (!joinOnCreate)
            return;

        Messages.sendCommandInfo(executor, "Joining game...");
        this.sessionManager.joinGame(executor, game);
    }

    @Subcommand("game join")
    public void onGameJoin(
            Player executor
    ) {
        if (this.sessionManager.isInGame(executor.getUniqueId()))
            throw new CommandErrorException("You cannot run that here!");

        new GameSelectGui(this.gameManager, this.sessionManager)
                .show(executor);
    }

    @Subcommand("game state next")
    @CommandPermission("bouncybullets.command.game.state")
    public void onGameStateNext(
            Player executor
    ) {
        BouncyBulletGame game = this.sessionManager.findGameSession(executor.getUniqueId())
                .orElseThrow(() -> new CommandErrorException("You are not in a game!"));

        Messages.sendCommandInfo(executor, "Switching to next state...");
        game.switchNextState();
    }

    @Subcommand("map reload")
    @CommandPermission("bouncybullets.command.map.reload")
    public void onMapReload(
            BukkitCommandActor actor
    ) {
        Messages.sendCommandInfo(actor.sender(), "Reloading maps...");

        this.mapManager.unregisterAllMaps();
        this.mapManager.loadGameMaps();

        Messages.sendCommandSuccess(actor.sender(), "Done!");
    }

    @Subcommand("map list")
    @CommandPermission("bouncybullets.command.map.list")
    public void onMapList(
            BukkitCommandActor actor
    ) {
        Component list =
                Component.join(
                        JoinConfiguration.builder()
                                .prefix(Component.text("\n\nRegistered Maps\n", NamedTextColor.GOLD).decorate(TextDecoration.BOLD))
                                .separator(Component.newline())
                                .build(),
                        this.mapManager.getGameMaps().entrySet().stream()
                                .map(entry -> Component.text("- ", NamedTextColor.DARK_GRAY)
                                        .append(Component.text(entry.getValue().displayName(), NamedTextColor.WHITE))
                                        .append(Component.text(" (" + entry.getKey() + ")", NamedTextColor.GRAY)))
                                .toList()
                );

        actor.sender().sendMessage(list);
    }

    @Subcommand("stats")
    public void onStats(
            BukkitCommandActor actor
    ) {
        PlayerStats stats = this.statsManager.getStatsOrThrow(actor.uniqueId());

        Component details =
                Component.join(
                        JoinConfiguration.builder()
                                .prefix(Component.text("Your Stats:\n", NamedTextColor.GOLD)
                                        .decorate(TextDecoration.BOLD))
                                .separator(Component.newline())
                                .build(),
                        List.of(
                                createStatText("Games played", stats.getGamesPlayed()),
                                createStatText("Total kills", stats.getTotalKills()),
                                createStatText("Total deaths", stats.getTotalDeaths()),
                                createStatText("Total wins", stats.getTotalWins()),
                                createStatText("Total losses", stats.getTotalLosses())
                        )
                );

        actor.sender().sendMessage(details);
    }

    private static Component createStatText(String stat, Object value) {
        return Component.text("  " + stat + ": ", NamedTextColor.WHITE)
                .append(Component.text(value.toString(), NamedTextColor.GREEN));

    }
}
