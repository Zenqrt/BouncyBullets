package dev.zenqrt.bouncybullets.game.games.states;

import dev.zenqrt.bouncybullets.event.PlayerJoinGameEvent;
import dev.zenqrt.bouncybullets.game.base.GameState;
import dev.zenqrt.bouncybullets.game.event.EventNode;
import dev.zenqrt.bouncybullets.game.event.GameEventNodes;
import dev.zenqrt.bouncybullets.game.event.PaperEventListener;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletPlayer;
import org.bukkit.event.player.PlayerEvent;

import java.util.Map;
import java.util.UUID;

public final class WaitingGameState extends GameState {

    private final EventNode<PlayerEvent> playerEventNode;

    private final PregameGameState pregameState;
    private final Map<UUID, BouncyBulletPlayer> players;
    private final int minPlayerCount;

    public WaitingGameState(PregameGameState pregameState, Map<UUID, BouncyBulletPlayer> players, int minPlayerCount) {
        this.pregameState = pregameState;
        this.players = players;
        this.minPlayerCount = minPlayerCount;

        this.playerEventNode = GameEventNodes.filteredPlayerEvents(pregameState.game);
    }

    private void registerEvents() {
        this.playerEventNode.registerListener(PaperEventListener.builder(PlayerJoinGameEvent.class)
                .filter(event -> event.getGame().getId() == pregameState.game.getId())
                .handler(event -> {
                    if (this.players.size() >= minPlayerCount) {
                        pregameState.switchNextState();
                    }
                }).build());
    }

    @Override
    protected void onStateStart() {
        registerEvents();
    }
}
