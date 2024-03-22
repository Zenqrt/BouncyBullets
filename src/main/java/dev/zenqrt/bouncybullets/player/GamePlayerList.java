package dev.zenqrt.bouncybullets.player;

import dev.zenqrt.bouncybullets.game.games.BouncyBulletPlayer;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Function;

public final class GamePlayerList extends HashMap<UUID, BouncyBulletPlayer> {

    public void updatePlayer(UUID uuid, Function<BouncyBulletPlayer, BouncyBulletPlayer> updateHandler) {
        this.replace(uuid, updateHandler.apply(this.get(uuid)));
    }

}
