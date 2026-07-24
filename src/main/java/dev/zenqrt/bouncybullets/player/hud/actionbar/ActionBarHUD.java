package dev.zenqrt.bouncybullets.player.hud.actionbar;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionBarHUD {

    private Component hudText;

    private final Map<String, Component> displays = new HashMap<>();
    private final List<String> displayOrder = new ArrayList<>();

    public ActionBarHUD() {
        this.hudText = Component.empty();
    }

    public void addDisplay(int index, String displayId, Component textDisplay) {
        this.displays.put(displayId, textDisplay);
        this.displayOrder.add(index, displayId);
    }

    public void addDisplay(String displayId, Component textDisplay) {
        this.displays.put(displayId, textDisplay);
        this.displayOrder.add(displayId);
    }

    public void removeDisplay(String displayId) {
        this.displays.remove(displayId);
        this.displayOrder.remove(displayId);
    }

    public boolean hasDisplay(String displayId) {
        return this.displays.containsKey(displayId);
    }

    public void updateDisplay(String displayId, Component textDisplay) {
        this.displays.put(displayId, textDisplay);
    }

    public void updateHudText() {
        this.hudText = createHudText();
    }

    public void show(Audience audience) {
        audience.sendActionBar(this.hudText);
    }

    private Component createHudText() {
        if (displays.isEmpty())
            return Component.empty();

        return Component.join(
                JoinConfiguration.builder()
                        .separator(Component.text(" | ", NamedTextColor.DARK_GRAY))
                        .build(),
                this.displayOrder.stream()
                        .map(this.displays::get)
                        .toList()
        );
    }

}
