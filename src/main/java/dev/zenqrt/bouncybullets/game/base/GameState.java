package dev.zenqrt.bouncybullets.game.base;

public abstract class GameState {

    private boolean active;
    private boolean canMoveOn;

    public GameState() {
        this.canMoveOn = true;
    }

    protected void onStateStart() {}
    protected void onStateEnd() {}

    public final boolean start() {
        if (active)
            return false;

        active = true;

        onStateStart();
        return true;
    }

    public final boolean end() {
        if (!(canMoveOn && active)) {
            return false;
        }

        active = false;

        onStateEnd();
        return true;
    }

    public boolean canMoveOn() {
        return canMoveOn;
    }

    public void setCanMoveOn(boolean canMoveOn) {
        this.canMoveOn = canMoveOn;
    }
}
