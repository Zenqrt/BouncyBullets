package dev.zenqrt.bouncybullets.map;

import org.bukkit.Location;
import org.bukkit.World;

public interface ActiveGameMap {
    World world();
    Location intermissionSpawn();
}
