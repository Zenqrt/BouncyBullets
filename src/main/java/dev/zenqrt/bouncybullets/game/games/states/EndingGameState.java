package dev.zenqrt.bouncybullets.game.games.states;

import dev.zenqrt.bouncybullets.game.EventGameState;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletPlayer;
import dev.zenqrt.bouncybullets.player.GamePlayerList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

import java.util.Comparator;

public final class EndingGameState extends EventGameState {

    private final GamePlayerList players;

    public EndingGameState(GamePlayerList players) {
        this.players = players;
    }

    @Override
    public void registerEvents() {
        players.forEach((uuid, player) -> {
            Player bukkitPlayer = player.player();

            bukkitPlayer.showTitle(Title.title(
                    Component.text("The winner is...", NamedTextColor.AQUA),
                    Component.text(getWinner().player().getName(), NamedTextColor.YELLOW)));
            bukkitPlayer.sendMessage(Component.text("The winner of the game is ", NamedTextColor.AQUA)
                    .append(Component.text(getWinner().player().getName(), NamedTextColor.YELLOW)));
        });
    }

    private BouncyBulletPlayer getWinner() {
        return players.values().stream()
                .max(Comparator.comparing(BouncyBulletPlayer::kills))
                .orElse(null);
    }
}
