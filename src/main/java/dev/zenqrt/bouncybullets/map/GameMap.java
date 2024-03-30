package dev.zenqrt.bouncybullets.map;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public record GameMap(String displayName, File worldFolder, YamlConfiguration configuration) {}
