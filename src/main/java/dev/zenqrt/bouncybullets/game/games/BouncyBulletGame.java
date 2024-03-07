package dev.zenqrt.bouncybullets.game.games;

import dev.zenqrt.bouncybullets.game.games.states.WaitingGameState;
import dev.zenqrt.bouncybullets.game.impl.PaperGame;
import dev.zenqrt.bouncybullets.game.impl.PaperGameEventHandler;
import dev.zenqrt.bouncybullets.game.impl.PaperGameState;

public final class BouncyBulletGame extends PaperGame {

    public BouncyBulletGame(int id) {
        super(id, createStartingGameState());
    }

    private static PaperGameState createStartingGameState() {
        return new WaitingGameState(new PaperGameEventHandler());
    }
}
