package dev.zenqrt.bouncybullets.game.games.states;

import dev.zenqrt.bouncybullets.game.base.GameState;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import dev.zenqrt.bouncybullets.player.GamePlayerList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

import java.util.Comparator;

public final class EndingGameState extends GameState {

    private final GamePlayerList players;

    public EndingGameState(GamePlayerList players) {
        this.players = players;
    }

    @Override
    protected void onStateStart() {
        players.showTitle(Title.title(
                Component.text("The winner is...", NamedTextColor.AQUA),
                Component.text(getWinner().player().getName(), NamedTextColor.YELLOW)));
        players.sendMessage(Component.text("The winner of the game is ", NamedTextColor.AQUA)
                .append(Component.text(getWinner().player().getName(), NamedTextColor.YELLOW)));
    }

    private BouncyBulletGamePlayer getWinner() {
        return players.values().stream()
                .max(Comparator.comparing(BouncyBulletGamePlayer::kills))
                .orElse(null);
    }
}
