package dev.zenqrt.bouncybullets.generator;

import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class VoidBiomeProvider extends BiomeProvider {

    @Override
    public @NotNull Biome getBiome(@NotNull WorldInfo worldInfo, int i, int i1, int i2) {
        return Biome.THE_VOID;
    }

    @Override
    public @NotNull List<Biome> getBiomes(@NotNull WorldInfo worldInfo) {
        return List.of(Biome.THE_VOID);
    }
}
