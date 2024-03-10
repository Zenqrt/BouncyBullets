package dev.zenqrt.bouncybullets.map;

import java.io.File;

public record FreeForAllGameMap(File folder, File worldFile) implements GameMap {
}
