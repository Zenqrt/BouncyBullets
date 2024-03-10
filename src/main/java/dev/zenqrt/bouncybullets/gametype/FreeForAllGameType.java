package dev.zenqrt.bouncybullets.gametype;

import dev.zenqrt.bouncybullets.game.games.BouncyBulletPlayer;
import dev.zenqrt.bouncybullets.map.FreeForAllGameMap;

import java.util.Map;
import java.util.UUID;

public final class FreeForAllGameType implements GameType {

    private final FreeForAllGameMap map;
    private final Map<UUID, BouncyBulletPlayer> players;

    public FreeForAllGameType(FreeForAllGameMap map, Map<UUID, BouncyBulletPlayer> players) {
        this.map = map;
        this.players = players;
    }

    @Override
    public void start() {
    }

    @Override
    public FreeForAllGameMap getMap() {
        return map;
    }
}
