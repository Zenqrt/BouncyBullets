package dev.zenqrt.bouncybullets.game.games.states;

import dev.zenqrt.bouncybullets.game.base.GameState;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import dev.zenqrt.bouncybullets.player.GamePlayerList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;

import java.util.Comparator;

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
        players.showTitle(Title.title(
                Component.text("The winner is...", NamedTextColor.AQUA),
                Component.text(getWinner().player().getName(), NamedTextColor.YELLOW)));
        players.sendMessage(Component.text("The winner of the game is ", NamedTextColor.AQUA)
                .append(Component.text(getWinner().player().getName(), NamedTextColor.YELLOW)));

        Bukkit.getScheduler().runTaskLater(
                this.game.getPlugin(),
                this.game::switchNextState,
                admirationTicks
        );
    }

    private BouncyBulletGamePlayer getWinner() {
        return players.values().stream()
                .max(Comparator.comparing(BouncyBulletGamePlayer::kills))
                .orElse(null);
    }
}
