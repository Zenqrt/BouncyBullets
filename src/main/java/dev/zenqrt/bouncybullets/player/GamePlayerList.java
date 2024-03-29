package dev.zenqrt.bouncybullets.player;

import dev.zenqrt.bouncybullets.game.games.BouncyBulletPlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Function;

public final class GamePlayerList extends HashMap<UUID, BouncyBulletPlayer> implements Audience {

    public BouncyBulletPlayer updatePlayer(UUID uuid, Function<BouncyBulletPlayer, BouncyBulletPlayer> updateHandler) {
        BouncyBulletPlayer newPlayer = updateHandler.apply(this.get(uuid));
        this.replace(uuid, newPlayer);

        return newPlayer;
    }

    @Override
    public void sendMessage(@NotNull Component message) {
        this.forEach((uuid, player) -> player.player().sendMessage(message));
    }
}
