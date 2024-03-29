package dev.zenqrt.bouncybullets.generator;

import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class VoidGenerator extends ChunkGenerator {

    @Override
    public BiomeProvider getDefaultBiomeProvider(@NotNull WorldInfo worldInfo) {
        return new VoidBiomeProvider();
    }

    @Override
    public boolean canSpawn(@NotNull World world, int x, int z) {
        return false;
    }

    private static class VoidBiomeProvider extends BiomeProvider {
        @Override
        public @NotNull Biome getBiome(@NotNull WorldInfo worldInfo, int i, int i1, int i2) {
            return Biome.THE_VOID;
        }

        @Override
        public @NotNull List<Biome> getBiomes(@NotNull WorldInfo worldInfo) {
            return List.of(Biome.THE_VOID);
        }
    }
}
