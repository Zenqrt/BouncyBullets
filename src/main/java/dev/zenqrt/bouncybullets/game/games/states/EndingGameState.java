package dev.zenqrt.bouncybullets.game.games.states;

import dev.zenqrt.bouncybullets.game.games.BouncyBulletPlayer;
import dev.zenqrt.bouncybullets.game.impl.PaperGameState;
import dev.zenqrt.bouncybullets.player.GamePlayerList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

import java.util.Comparator;

public final class EndingGameState extends PaperGameState {

    private final GamePlayerList players;

    public EndingGameState(GamePlayerList players) {
        this.players = players;
    }

    @Override
    public void registerEvents() {
        players.forEach((uuid, player) ->
                player.player().showTitle(Title.title(
                        Component.text("The winner is...", NamedTextColor.AQUA),
                        Component.text(getWinner().player().getName(), NamedTextColor.YELLOW))));
    }

    private BouncyBulletPlayer getWinner() {
        return players.values().stream()
                .max(Comparator.comparing(BouncyBulletPlayer::kills))
                .orElse(null);
    }
}
