package dev.zenqrt.bouncybullets.game;

public class Game<E> {

    private final int id;
    private GameState<E> gameState;

    public Game(int id, GameState<E> startingGameState) {
        this.id = id;
        this.gameState = startingGameState;
    }

    public final void start() {
        gameState.start();
    }

    public int getId() {
        return id;
    }

    public GameState<E> getGameState() {
        return gameState;
    }

    public void setGameState(GameState<E> gameState) {
        this.gameState = gameState;
    }
}
