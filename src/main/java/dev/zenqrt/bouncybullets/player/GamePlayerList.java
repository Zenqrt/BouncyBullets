package dev.zenqrt.bouncybullets.player;

import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public final class GamePlayerList extends HashMap<UUID, BouncyBulletGamePlayer> implements Audience {

    @Override
    public void sendMessage(@NotNull Component message) {
        this.forEach((_, player) -> player.getPlayer().sendMessage(message));
    }

    @Override
    public void playSound(@NotNull Sound sound, Sound.Emitter emitter) {
        this.forEach((_, player) -> player.getPlayer().playSound(sound, emitter));
    }

    @Override
    public void showTitle(@NotNull Title title) {
        this.forEach((_, player) -> player.getPlayer().showTitle(title));
    }
}
