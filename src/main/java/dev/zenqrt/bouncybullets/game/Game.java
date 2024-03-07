package dev.zenqrt.bouncybullets.game;

public class Game<S extends GameState<?>> {

    private final int id;
    private S gameState;

    public Game(int id, S startingGameState) {
        this.id = id;
        this.gameState = startingGameState;
    }

    public final void start() {
        gameState.start();
    }

    public int getId() {
        return id;
    }

    public S getGameState() {
        return gameState;
    }

    public void setGameState(S gameState) {
        this.gameState = gameState;
    }
}
