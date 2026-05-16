package dev.zenqrt.bouncybullets.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

public final class YamlParser {

    private YamlParser() {}

    public static Location parsePositionInWorld(YamlConfiguration config, String path, World world) {
        double x = config.getDouble(path + ".x");
        double y = config.getDouble(path + ".y");
        double z = config.getDouble(path + ".z");

        return new Location(world, x, y, z);
    }

}
