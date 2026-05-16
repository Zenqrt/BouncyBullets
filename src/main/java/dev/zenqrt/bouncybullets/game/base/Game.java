package dev.zenqrt.bouncybullets.game.base;

public class Game extends GameStateSequence {

    private final int id;

    public Game(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public GameState getGameState() {
        return this.states.get(this.getCurrentStateIndex());
    }
}
