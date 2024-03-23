package dev.zenqrt.bouncybullets.player;

import dev.zenqrt.bouncybullets.game.games.BouncyBulletPlayer;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Function;

public final class GamePlayerList extends HashMap<UUID, BouncyBulletPlayer> {

    public BouncyBulletPlayer updatePlayer(UUID uuid, Function<BouncyBulletPlayer, BouncyBulletPlayer> updateHandler) {
        BouncyBulletPlayer newPlayer = updateHandler.apply(this.get(uuid));
        this.replace(uuid, newPlayer);

        return newPlayer;
    }

}
