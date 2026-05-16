package dev.zenqrt.bouncybullets.player;

import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Function;

public final class GamePlayerList extends HashMap<UUID, BouncyBulletGamePlayer> implements Audience {

    public BouncyBulletGamePlayer updatePlayer(UUID uuid, Function<BouncyBulletGamePlayer, BouncyBulletGamePlayer> updateHandler) {
        BouncyBulletGamePlayer newPlayer = updateHandler.apply(this.get(uuid));
        this.replace(uuid, newPlayer);

        return newPlayer;
    }

    @Override
    public void sendMessage(@NotNull Component message) {
        this.forEach((uuid, player) -> player.player().sendMessage(message));
    }

    @Override
    public void playSound(@NotNull Sound sound, Sound.Emitter emitter) {
        this.forEach((uuid, player) -> player.player().playSound(sound, emitter));
    }

    @Override
    public void showTitle(@NotNull Title title) {
        this.forEach((uuid, player) -> player.player().showTitle(title));
    }
}
