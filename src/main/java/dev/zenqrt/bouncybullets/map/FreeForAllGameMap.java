package dev.zenqrt.bouncybullets.map;

import org.bukkit.Location;

import java.io.File;
import java.util.List;

public record FreeForAllGameMap(File folder, File worldFile, List<Location> spawnLocations, List<Location> itemSpawnLocations) implements GameMap {
}
