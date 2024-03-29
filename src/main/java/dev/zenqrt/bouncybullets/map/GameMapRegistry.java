package dev.zenqrt.bouncybullets.map;

import com.google.common.base.Preconditions;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class GameMapRegistry {

    private static final Map<String, GameMap> gameMaps = new HashMap<>();

    public static void registerGameMaps(File mapFolder) {
        if (!mapFolder.exists()) {
            mapFolder.mkdirs();
        }

        File[] folders = Preconditions.checkNotNull(mapFolder.listFiles(), "Specified map folder is not a directory.");

        for (File file : folders) {
            if (file.isDirectory()) {
                gameMaps.put(file.getName(), new GameMap(new File(file, "config.yml"), new File(file, "world")));
            }
        }
    }

    public static GameMap getGameMap(String name) {
        return gameMaps.get(name);
    }

    public static Map<String, GameMap> getGameMaps() {
        return Collections.unmodifiableMap(gameMaps);
    }

}
