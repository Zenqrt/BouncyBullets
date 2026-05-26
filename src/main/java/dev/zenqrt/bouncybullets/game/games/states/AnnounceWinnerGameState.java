package dev.zenqrt.bouncybullets.game.games.states;

import dev.zenqrt.bouncybullets.game.base.GameState;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import dev.zenqrt.bouncybullets.player.GamePlayerList;
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
    private final GamePlayerList players;
    private final BouncyBulletGame game;

    public AnnounceWinnerGameState(BouncyBulletGame game, GamePlayerList players, int admirationTicks) {
        this.game = game;
        this.players = players;
        this.admirationTicks = admirationTicks;
    }

    @Override
    protected void onStateStart() {
        String winnerName = getWinner().getPlayer().getName();

        List<Component> topKillersLines = new ArrayList<>();
        List<BouncyBulletGamePlayer> topKillers = this.players.values().stream()
                .sorted(Comparator.comparingInt(BouncyBulletGamePlayer::getKills).reversed())
                .toList();

        for (int i = 0; i < topKillers.size(); i++) {
            BouncyBulletGamePlayer gamePlayer = topKillers.get(i);

            topKillersLines.add(
                    Component.text((i + 1) + ". ", NamedTextColor.WHITE)
                            .append(Component.text(gamePlayer.getPlayer().getName(), NamedTextColor.AQUA))
                            .append(Component.text(" - ", NamedTextColor.GRAY))
                            .append(Component.text(gamePlayer.getKills(), NamedTextColor.YELLOW))
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

        this.players.sendMessage(leaderboard);

        this.players.showTitle(Title.title(
                Component.text("The winner is...", NamedTextColor.AQUA),
                Component.text(winnerName, NamedTextColor.YELLOW)));
        this.players.sendMessage(
                Component.empty()
                        .append(Component.text("\nWinner: ", NamedTextColor.GOLD).decorate(TextDecoration.BOLD))
                        .append(Component.text(winnerName, NamedTextColor.WHITE))
        );

        Bukkit.getScheduler().runTaskLater(
                this.game.getPlugin(),
                this.game::switchNextState,
                admirationTicks
        );
    }

    private BouncyBulletGamePlayer getWinner() {
        return players.values().stream()
                .max(Comparator.comparing(BouncyBulletGamePlayer::getKills))
                .orElse(null);
    }
}
