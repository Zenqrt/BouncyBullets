package dev.zenqrt.bouncybullets.game.games.states;

import dev.zenqrt.bouncybullets.game.GameManager;
import dev.zenqrt.bouncybullets.game.base.GameState;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import dev.zenqrt.bouncybullets.lobby.LobbyManager;
import org.bukkit.entity.Player;

public final class SendPlayersToLobbyGameState extends GameState {

    private final LobbyManager lobbyManager;
    private final GameManager gameManager;
    private final BouncyBulletGame game;

    public SendPlayersToLobbyGameState(BouncyBulletGame game, GameManager gameManager, LobbyManager lobbyManager) {
        this.game = game;
        this.gameManager = gameManager;
        this.lobbyManager = lobbyManager;
    }

    @Override
    protected void onStateStart() {
        for (BouncyBulletGamePlayer gamePlayer : this.game.getPlayers().values()) {
            Player player = gamePlayer.getPlayer();

            this.lobbyManager.sendToLobby(player);
            this.gameManager.leaveGame(player, this.game);
        }

        this.game.switchNextState();
    }
}
