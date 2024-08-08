package dev.zenqrt.bouncybullets.game.base;

import java.util.ArrayList;
import java.util.List;

public abstract class GameStateSequence extends GameState {

    private final List<GameState> states;
    private int currentStateIndex;

    public GameStateSequence() {
        this.states = new ArrayList<>();
        this.currentStateIndex = 0;
    }

    protected void onLastStateFinished() {}

    @Override
    protected void onStateStart() {
        if (states.isEmpty()) {
            end();
            return;
        }

        states.get(currentStateIndex).start();
    }

    @Override
    protected void onStateEnd() {
        if (currentStateIndex >= states.size())
            return;

        states.get(currentStateIndex).end();
    }

    public void switchNextState() {
        if (currentStateIndex >= states.size() - 1) {
            onLastStateFinished();
            return;
        }

        GameState currentState = states.get(currentStateIndex);

        if (!currentState.end())
            return;

        states.get(++currentStateIndex).start();
    }

    public void switchPreviousState() {
        GameState currentState = states.get(currentStateIndex);

        if (!currentState.end())
            return;

        states.get(--currentStateIndex).start();
    }

    protected void addState(GameState state) {
        states.add(state);
    }

}
