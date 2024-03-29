package dev.zenqrt.bouncybullets.gametype;

import dev.zenqrt.bouncybullets.map.ActiveGameMap;

public interface GameType {

    void start();
    ActiveGameMap getMap();

}
