package dev.zenqrt.bouncybullets.game.games.states;

import dev.zenqrt.bouncybullets.game.impl.PaperGameState;
import dev.zenqrt.bouncybullets.generator.VoidBiomeProvider;
import dev.zenqrt.bouncybullets.generator.VoidGenerator;
import dev.zenqrt.bouncybullets.map.GameMap;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;

import java.io.File;
import java.io.IOException;

public final class SetupMapGameState extends PaperGameState {


    private final PregameGameState gameState;
    private final int gameId;
    private final GameMap gameMap;

    public SetupMapGameState(PregameGameState gameState, int gameId, GameMap gameMap) {
        this.gameState = gameState;
        this.gameId = gameId;
        this.gameMap = gameMap;
    }

    @Override
    public void registerEvents() {

    }

    @Override
    protected void onStateStart() {
        String worldName = "game_world_" + gameId;

        try {
            FileUtils.copyDirectory(gameMap.worldFolder(), new File(Bukkit.getWorldContainer().getParentFile(), worldName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Bukkit.createWorld(new WorldCreator(worldName)
                .generateStructures(false)
                .generator(new VoidGenerator())
                .biomeProvider(new VoidBiomeProvider()));
        this.gameState.switchNextState();
    }
}
