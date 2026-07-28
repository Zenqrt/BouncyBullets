package dev.zenqrt.bouncybullets.hud;

import dev.zenqrt.bouncybullets.utils.FontMetrics;
import dev.zenqrt.bouncybullets.utils.ResourceKeys;
import dev.zenqrt.bouncybullets.utils.Space;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.ShadowColor;

public final class HealthBarElement implements HudElement {

    private static int calculateCenterTextBackgroundSpace(int bgImageWidth, int textAreaWidth) {
        int textAreaMidpoint = (textAreaWidth / 2) + 1;
        int halfBgWidth = bgImageWidth / 2;

        return halfBgWidth - bgImageWidth - textAreaMidpoint;
    }

    private static TextComponent buildHealthBar(int health, int maxHealth) {
        int green = (int) (100F * health / maxHealth);

        return Component.text("|".repeat(green), NamedTextColor.GREEN)
                .append(Component.text("|".repeat(100 - green), NamedTextColor.DARK_GRAY))
                .shadowColor(ShadowColor.shadowColor(0))
                .font(ResourceKeys.hudFont("health_bar"));
    }

    private static TextComponent healthComponent(int health, int maxHealth) {
        TextComponent healthBar = buildHealthBar(health, maxHealth);
        TextComponent healthText = Component.text(health, NamedTextColor.WHITE)
                .append(Component.text("/" + maxHealth, NamedTextColor.GRAY));

        int healthBarWidth = FontMetrics.width(healthBar);
        int centerSpace = calculateCenterTextBackgroundSpace(
                healthBarWidth,
                FontMetrics.width(healthText)
        );

        return Component.text()
                .append(healthBar)
                .append(Space.of(centerSpace))
                .append(healthText)
                .append(Space.of(healthBarWidth + centerSpace))
                .shadowColor(ShadowColor.shadowColor(0))
                .font(ResourceKeys.hudFont("health_big"))
                .build();
    }

    private int health;
    private int maxHealth;

    @Override
    public Component render() {
        return healthComponent(this.health, this.maxHealth);
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }
}
