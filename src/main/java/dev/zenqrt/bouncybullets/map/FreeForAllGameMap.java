package dev.zenqrt.bouncybullets.map;

import org.bukkit.util.Vector;

import java.io.File;
import java.util.List;

public record FreeForAllGameMap(File folder, File worldFolder, List<Vector> spawnLocations, List<Vector> itemSpawnLocations) implements GameMap {



}
