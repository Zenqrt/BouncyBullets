package dev.zenqrt.bouncybullets.game.games.states;

import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletPlayer;
import dev.zenqrt.bouncybullets.game.impl.PaperGameState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PregameGameState extends PaperGameState {

    private final List<PaperGameState> states;

    public PregameGameState(BouncyBulletGame game, Map<UUID, BouncyBulletPlayer> players) {
        WaitingGameState waitingState = new WaitingGameState(game, players);
        this.states = List.of(waitingState, new CountdownGameState(game, players, waitingState));
    }

    @Override
    public void registerEvents() {

    }

    @Override
    protected void onStateStart() {

    }
}
