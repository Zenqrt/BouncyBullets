package dev.zenqrt.bouncybullets.player.hud;

import com.google.common.base.Preconditions;
import dev.zenqrt.bouncybullets.loadout.kit.PlayerClassType;
import dev.zenqrt.bouncybullets.utils.FontMetrics;
import dev.zenqrt.bouncybullets.utils.ResourceKeys;
import dev.zenqrt.bouncybullets.utils.Space;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.title.Title;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

public final class GameplayHud {

    private static final Component GAME_TIMER_BACKGROUND = Component.text('\uE000')
            .font(Key.key("bouncybullets", "game"))
            .shadowColor(ShadowColor.shadowColor(0));

    private final Set<Audience> viewers = new HashSet<>();

    private int ammo;
    private int maxAmmo;
    private boolean infiniteAmmo;
    private boolean showAmmo;
    private int health;
    private int maxHealth;
    private @Nullable PlayerClassType classType;
    private Component statusText;
    private final BossBar gameTimerHud;

    public GameplayHud() {
        this.gameTimerHud = BossBar.bossBar(
                gameTimerComponent(743),
                0,
                BossBar.Color.WHITE,
                BossBar.Overlay.PROGRESS
        );

        this.health = 0;
        this.maxHealth = 0;
        this.classType = null;
        this.statusText = Component.empty();
    }

    private static Component gameTimerComponent(int timeSeconds) {
        String formattedTime = getGameTimeFormatted(timeSeconds);
        int centerSpace = calculateCenterTextBackgroundSpace(45, FontMetrics.width(formattedTime));

        return Component.text()
                .append(GAME_TIMER_BACKGROUND)
                .append(Space.of(centerSpace))
                .append(
                        Component.text(formattedTime, NamedTextColor.YELLOW)
                                .font(Key.key("bouncybullets", "hud/game_timer"))
                                .shadowColor(ShadowColor.shadowColor(0))
                )
                .append(Space.of(45 + centerSpace))
                .build();
    }

    private static String getGameTimeFormatted(int timeSeconds) {
        int minutes = timeSeconds / 60;
        int seconds = timeSeconds % 60;

        StringBuilder builder = new StringBuilder(5);

        if (minutes < 10)
            builder.append('0');

        builder.append(minutes);
        builder.append(':');

        if (seconds < 10)
            builder.append('0');

        builder.append(seconds);

        return builder.toString();
    }

    private static int calculateCenterTextBackgroundSpace(int bgImageWidth, int textAreaWidth) {
        int textAreaMidpoint = (textAreaWidth / 2) + 1;
        int halfBgWidth = bgImageWidth / 2;

        return halfBgWidth - bgImageWidth - textAreaMidpoint;
    }

    public void tick() {
        TextComponent classInfoText = classInfoComponent(this.classType);
        int classInfoWidth = FontMetrics.width(classInfoText);

        final TextComponent.Builder builder =
                Component.text()
                        .append(Space.of(-100))
                        .append(Space.of(-classInfoWidth))
                        .append(classInfoText)
                        .append(Space.of(classInfoWidth))
                        .append(healthComponent(this.health, this.maxHealth));


        if (this.showAmmo) {
            TextComponent ammoText = infiniteAmmoComponent(this.ammo);
            int ammoWidth = FontMetrics.width(ammoText);

            builder.append(Space.of(100))
                    .append(ammoText)
                    .append(Space.of(-101 - ammoWidth));
        }

        Component hudText = builder.build();
        Title statusTitle = Title.title(
                Component.empty(),
                this.statusText,
                Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ZERO)
        );

        for (Audience viewer : this.viewers) {
            viewer.sendActionBar(hudText);
            viewer.showTitle(statusTitle);
        }
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

    private static TextComponent infiniteAmmoComponent(int ammo) {
        return Component.text()
                .append(buildInfiniteAmmoText(ammo))
                .build();
    }

    private static TextComponent buildInfiniteAmmoText(int ammo) {
        return Component.text()
                .append(Component.text(ammo))
                .append(Component.text("  |  ∞", NamedTextColor.GRAY))
                .shadowColor(ShadowColor.shadowColor(0))
                .font(ResourceKeys.hudFont("ammo"))
                .build();
    }

    private static TextComponent classInfoComponent(PlayerClassType classType) {
        String className = classType == null ? "None" : classType.getPlayerClass().getName();

        return Component.text("Class: " + className)
                .shadowColor(ShadowColor.shadowColor(0))
                .font(ResourceKeys.hudFont("ammo"));
    }

    public void setGameTimer(int timeSeconds) {
        this.gameTimerHud.name(gameTimerComponent(timeSeconds));
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public void setAmmo(int ammo) {
        this.ammo = ammo;
    }

    public void showAmmo() {
        Preconditions.checkArgument(!this.showAmmo, "Ammo already shown");

        this.showAmmo = true;
    }

    public void hideAmmo() {
        Preconditions.checkArgument(this.showAmmo, "Ammo already hidden");

        this.showAmmo = false;
    }

    public void setPlayerClassType(PlayerClassType classType) {
        this.classType = classType;
    }

    public void setStatus(Component statusText) {
        this.statusText = statusText;
    }

    public void clearStatus() {
        this.statusText = Component.empty();
    }

    public void display(Audience audience) {
        audience.showBossBar(this.gameTimerHud);

        this.viewers.add(audience);
    }

    public void hide(Audience audience) {
        audience.hideBossBar(this.gameTimerHud);

        this.viewers.remove(audience);
    }
}
