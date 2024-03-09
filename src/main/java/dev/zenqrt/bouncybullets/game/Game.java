package dev.zenqrt.bouncybullets.game;

public class Game<S extends GameState<?>> {

    private final int id;
    protected S gameState;

    public Game(int id, S startingGameState) {
        this.id = id;
        this.gameState = startingGameState;
    }

    public void start() {
        gameState.start();
    }

    public int getId() {
        return id;
    }

    public S getGameState() {
        return gameState;
    }

    public void switchGameState(S gameState) {
        this.gameState.end();
        this.gameState = gameState;

        gameState.start();
    }
}
