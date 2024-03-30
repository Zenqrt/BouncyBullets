package dev.zenqrt.bouncybullets.map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;
import java.util.stream.Collectors;

public record FreeForAllActiveGameMap(World world, List<Location> spawnLocations, List<Location> itemSpawnLocations) implements ActiveGameMap {

    public FreeForAllActiveGameMap(World world, YamlConfiguration configuration) {
        this(world, parseLocations(world, configuration, "spawn-locations"), parseLocations(world, configuration, "item-spawn-locations"));
    }

    private static List<Location> parseLocations(World world, YamlConfiguration configuration, String key) {
        if (configuration.get(key) == null)
            return List.of();

        return configuration.getMapList(key).stream()
                .map(map -> {
                    double x = (double) map.get("x");
                    double y = (double) map.get("y");
                    double z = (double) map.get("z");
                    double yaw = (double) map.get("yaw");
                    double pitch = (double) map.get("pitch");

                    return new Location(world, x, y, z, (float) yaw, (float) pitch);
                }).collect(Collectors.toList());
    }

}
