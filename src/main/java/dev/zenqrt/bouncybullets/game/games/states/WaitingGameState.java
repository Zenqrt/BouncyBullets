package dev.zenqrt.bouncybullets.game.games.states;

import dev.zenqrt.bouncybullets.event.PlayerJoinGameEvent;
import dev.zenqrt.bouncybullets.game.EventGameState;
import dev.zenqrt.bouncybullets.game.event.impl.PaperEventListener;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletPlayer;

import java.util.Map;
import java.util.UUID;

public final class WaitingGameState extends EventGameState {

    private final PregameGameState pregameState;
    private final Map<UUID, BouncyBulletPlayer> players;
    private final int minPlayerCount;

    public WaitingGameState(PregameGameState pregameState, Map<UUID, BouncyBulletPlayer> players, int minPlayerCount) {
        this.pregameState = pregameState;
        this.players = players;
        this.minPlayerCount = minPlayerCount;
    }

    @Override
    public void registerEvents() {
        this.eventNode.registerListener(PaperEventListener.builder(PlayerJoinGameEvent.class)
                .filter(event -> event.getGame().getId() == pregameState.game.getId())
                .handler(event -> {
                    if (this.players.size() >= minPlayerCount) {
                        pregameState.switchNextState();
                    }
                }).build());
    }

}
