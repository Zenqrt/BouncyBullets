package dev.zenqrt.bouncybullets.gametype;

import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import dev.zenqrt.bouncybullets.map.FreeForAllActiveGameMap;

import java.util.Map;
import java.util.UUID;

public final class FreeForAllGameType implements GameType {

    private final FreeForAllActiveGameMap map;
    private final Map<UUID, BouncyBulletGamePlayer> players;

    public FreeForAllGameType(FreeForAllActiveGameMap map, Map<UUID, BouncyBulletGamePlayer> players) {
        this.map = map;
        this.players = players;
    }

    @Override
    public void start() {
    }

    @Override
    public FreeForAllActiveGameMap getMap() {
        return map;
    }
}
