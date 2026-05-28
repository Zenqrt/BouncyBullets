package dev.zenqrt.bouncybullets.game.games.states;

import dev.zenqrt.bouncybullets.game.base.GameState;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import dev.zenqrt.bouncybullets.game.games.GamePlayerList;
import dev.zenqrt.bouncybullets.stats.PlayerStatsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class AnnounceWinnerGameState extends GameState {

    private final int admirationTicks;
    private final PlayerStatsManager statsManager;
    private final BouncyBulletGame game;

    public AnnounceWinnerGameState(BouncyBulletGame game, PlayerStatsManager statsManager, int admirationTicks) {
        this.game = game;
        this.statsManager = statsManager;
        this.admirationTicks = admirationTicks;
    }

    @Override
    protected void onStateStart() {
        GamePlayerList players = this.game.getPlayers();

        List<Component> topKillersLines = new ArrayList<>();
        List<BouncyBulletGamePlayer> topKillers = players.values().stream()
                .sorted(Comparator.comparingInt(BouncyBulletGamePlayer::getKills).reversed())
                .toList();

        BouncyBulletGamePlayer winner = topKillers.getFirst();
        String winnerName = winner.getPlayer().getName();

        this.statsManager.recordWin(winner);
        topKillersLines.add(
                createPlacementText(1, winner)
        );

        for (int i = 1; i < topKillers.size(); i++) {
            BouncyBulletGamePlayer gamePlayer = topKillers.get(i);

            this.statsManager.recordLoss(gamePlayer);

            topKillersLines.add(
                    createPlacementText(i + 1, gamePlayer)
            );
        }

        Component leaderboard =
                Component.join(
                        JoinConfiguration.builder()
                                .prefix(Component.text("\n\n\nKills Leaderboard:\n", NamedTextColor.RED).decorate(TextDecoration.BOLD))
                                .separator(Component.newline())
                                .build(),
                        topKillersLines
                );

        players.sendMessage(leaderboard);

        players.showTitle(Title.title(
                Component.text("The winner is...", NamedTextColor.AQUA),
                Component.text(winnerName, NamedTextColor.YELLOW)));
        players.sendMessage(
                Component.empty()
                        .append(Component.text("\nWinner: ", NamedTextColor.GOLD).decorate(TextDecoration.BOLD))
                        .append(Component.text(winnerName, NamedTextColor.WHITE))
        );

        Bukkit.getScheduler().runTaskLater(
                this.game.getPlugin(),
                this.game::switchNextState,
                this.admirationTicks
        );
    }

    private static Component createPlacementText(int placement, BouncyBulletGamePlayer gamePlayer) {
        return Component.text(placement + ". ", NamedTextColor.WHITE)
                .append(Component.text(gamePlayer.getPlayer().getName(), NamedTextColor.AQUA))
                .append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text(gamePlayer.getKills(), NamedTextColor.YELLOW));
    }

}
