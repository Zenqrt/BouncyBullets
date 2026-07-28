package dev.zenqrt.bouncybullets.hud;

public final class HudComponent {

    private final HudElement hudElement;
    private final HudAlignment hudAlignment;
    private int horizontalOffset;

    public HudComponent(HudElement hudElement, int horizontalOffset, HudAlignment alignment) {
        this.hudElement = hudElement;
        this.horizontalOffset = horizontalOffset;
        this.hudAlignment = alignment;
    }

    public HudElement getHudElement() {
        return hudElement;
    }

    public HudAlignment getHudAlignment() {
        return hudAlignment;
    }

    public void setHorizontalOffset(int horizontalOffset) {
        this.horizontalOffset = horizontalOffset;
    }

    public int getHorizontalOffset() {
        return horizontalOffset;
    }
}
