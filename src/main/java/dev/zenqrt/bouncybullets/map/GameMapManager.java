package dev.zenqrt.bouncybullets.map;

import com.google.common.base.Preconditions;
import dev.zenqrt.bouncybullets.BouncyBulletsPlugin;
import dev.zenqrt.bouncybullets.generator.VoidGenerator;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class GameMapManager {

    private final Map<String, GameMap> gameMaps = new HashMap<>();
    private final Map<Integer, World> gameWorlds = new HashMap<>();
    private final List<String> gameMapIds = new ArrayList<>();
    private final File mapsFolder;
    private final BouncyBulletsPlugin plugin;

    public GameMapManager(BouncyBulletsPlugin plugin, File mapsFolder) {
        this.plugin = plugin;
        this.mapsFolder = mapsFolder;
    }

    public void loadGameMaps() {
        if (!this.mapsFolder.exists() && !this.mapsFolder.mkdirs())
            throw new IllegalStateException("Could not create map folder at " + this.mapsFolder.getAbsolutePath());

        File[] folders = Preconditions.checkNotNull(this.mapsFolder.listFiles(), "Specified map folder is not a directory.");

        for (File directory : folders) {
            if (directory.isDirectory()) {
                YamlConfiguration configuration = YamlConfiguration.loadConfiguration(new File(directory, "config.yml"));
                String displayName = configuration.getString("DisplayName");
                File worldFolder = new File(directory, "world");

                registerMap(directory.getName(), new GameMap(displayName, worldFolder, configuration));
            }
        }
    }

    public void registerMap(String id, GameMap map) {
        this.gameMaps.put(id, map);
        this.gameMapIds.add(id);

        this.plugin.getSLF4JLogger().info("Registered map '{}'", id);
    }

    public void unregisterAllMaps() {
        this.gameMaps.clear();
        this.gameMapIds.clear();
    }

    public void createGameWorldAsync(int gameId, GameMap map, Consumer<World> onComplete) {
        String worldName = "game_world_" + gameId;

        CompletableFuture.runAsync(
                () -> {
                    try {
                        FileUtils.copyDirectory(map.worldFolder(), new File(Bukkit.getWorldContainer().getParentFile(), worldName));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        ).thenRun(
                () -> Bukkit.getScheduler().runTask(this.plugin, () -> {
                    World world = Bukkit.createWorld(new WorldCreator(worldName)
                            .generator(new VoidGenerator()));

                    if (world == null) {
                        throw new RuntimeException("Failed to load world: " + worldName);
                    }

                    world.setAutoSave(false);
                    this.gameWorlds.put(gameId, world);

                    onComplete.accept(world);
                })
        );
    }

    public void deleteGameWorld(int gameId, World world) {
        unloadAndDeleteWorld(world);

        if (!this.gameWorlds.remove(gameId, world))
            throw new RuntimeException("Could not remove world '" + world.getName() + "' from game worlds map");
    }

    public void deleteAllGameWorlds() {
        this.gameWorlds.forEach((_, world) -> unloadAndDeleteWorld(world));
        this.gameWorlds.clear();
    }

    private static void unloadAndDeleteWorld(World world) {
        if (Bukkit.getWorlds().contains(world) && !Bukkit.unloadWorld(world, false))
            throw new RuntimeException("Could not unload world '" + world.getName() + "'");

        try {
            FileUtils.deleteDirectory(world.getWorldFolder());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Optional<GameMap> findGameMap(String mapId) {
        GameMap map = this.gameMaps.get(mapId);

        return map == null ? Optional.empty() : Optional.of(map);
    }

    public Map<String, GameMap> getGameMaps() {
        return Collections.unmodifiableMap(gameMaps);
    }

    public List<String> getGameMapIds() {
        return gameMapIds;
    }
}
