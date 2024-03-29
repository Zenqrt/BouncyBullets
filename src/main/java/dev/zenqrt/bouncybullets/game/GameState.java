package dev.zenqrt.bouncybullets.game;

public abstract class GameState {

    private boolean active;
    private boolean canMoveOn;

    public GameState() {
        this.canMoveOn = true;
    }

    protected void onStateStart() {}
    protected void onStateEnd() {}

    public final void start() {
        if (active)
            return;

        active = true;

        onStateStart();
    }

    public final void end() {
        if (!canMoveOn || !active)
            return;

        active = false;

        onStateEnd();
    }

    public boolean canMoveOn() {
        return canMoveOn;
    }

    public void setCanMoveOn(boolean canMoveOn) {
        this.canMoveOn = canMoveOn;
    }
}
