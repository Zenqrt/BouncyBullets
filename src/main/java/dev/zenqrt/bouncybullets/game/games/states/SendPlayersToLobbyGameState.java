package dev.zenqrt.bouncybullets.game.games.states;

import dev.zenqrt.bouncybullets.game.base.GameState;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import dev.zenqrt.bouncybullets.player.PlayerSessionManager;
import org.bukkit.entity.Player;

import java.util.List;

public final class SendPlayersToLobbyGameState extends GameState {

    private final PlayerSessionManager sessionManager;
    private final BouncyBulletGame game;

    public SendPlayersToLobbyGameState(BouncyBulletGame game, PlayerSessionManager sessionManager) {
        this.game = game;
        this.sessionManager = sessionManager;
    }

    @Override
    protected void onStateStart() {
        List<Player> players = this.game.getPlayers().values().stream()
                .map(BouncyBulletGamePlayer::getPlayer)
                .toList();

        for (Player player : players) {
            this.sessionManager.joinLobby(player, true);
        }

        this.game.switchNextState();
    }
}
