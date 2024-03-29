package dev.zenqrt.bouncybullets.game;

public class Game extends GameState {

    private final int id;
    protected GameState gameState;

    public Game(int id, GameState startingGameState) {
        this.id = id;
        this.gameState = startingGameState;
    }

    @Override
    protected void onStateStart() {
        gameState.start();
    }

    @Override
    protected void onStateEnd() {
        gameState.end();
    }

    public int getId() {
        return id;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void switchGameState(GameState gameState) {
        this.gameState.end();
        this.gameState = gameState;

        gameState.start();
    }
}
