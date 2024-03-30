package dev.zenqrt.bouncybullets.game.games.states;

import dev.zenqrt.bouncybullets.event.PlayerJoinGameEvent;
import dev.zenqrt.bouncybullets.game.GameStateSequence;
import dev.zenqrt.bouncybullets.game.event.impl.PaperEventListener;
import dev.zenqrt.bouncybullets.game.event.impl.PaperEventNode;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.generator.VoidGenerator;
import dev.zenqrt.bouncybullets.item.GameItem;
import dev.zenqrt.bouncybullets.item.items.LoadoutGameItem;
import dev.zenqrt.bouncybullets.item.items.VoteMapGameItem;
import dev.zenqrt.bouncybullets.map.FreeForAllActiveGameMap;
import dev.zenqrt.bouncybullets.map.GameMap;
import dev.zenqrt.bouncybullets.map.GameMapRegistry;
import dev.zenqrt.bouncybullets.player.GamePlayerList;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PregameGameState extends GameStateSequence {

    private static final int MIN_PLAYER_COUNT = 2;
    private static final int MAX_PLAYER_COUNT = 8;
    private static final Location SPAWN_LOCATION = new Location(Bukkit.getWorld("world"), 213.5, 66, 84.5);
    private static final LoadoutGameItem LOADOUT_ITEM = new LoadoutGameItem();

    private final PaperEventNode<Event> eventNode = new PaperEventNode<>();
    private final Map<GameMap, Integer> mapVotes = new HashMap<>();
    private final VoteMapGameItem voteMapItem = new VoteMapGameItem(mapVotes);
    private final GamePlayerList players;
    final BouncyBulletGame game;

    public PregameGameState(BouncyBulletGame game, GamePlayerList players) {
        this.game = game;
        this.players = players;

        GameMapRegistry.getGameMaps().forEach((key, value) -> mapVotes.put(value, 0));

        this.addState(new WaitingGameState(this, players, MIN_PLAYER_COUNT));
        this.addState(new CountdownGameState(this, players, MIN_PLAYER_COUNT));
    }

    public void registerEvents() {
        GameItem.registerGameItemEvents(List.of(LOADOUT_ITEM, new VoteMapGameItem(mapVotes)));

        this.eventNode.registerListener(PaperEventListener.builder(PlayerDropItemEvent.class)
                .handler(event -> event.setCancelled(true))
                .build());
        this.eventNode.registerListener(PaperEventListener.builder(PlayerJoinGameEvent.class)
                .filter(event -> event.getGame().getId() == game.getId())
                .handler(event -> {
                    players.sendMessage(MiniMessage.miniMessage().deserialize("<green>{player} joined the game! ({playerCount}/{maxPlayers})")
                            .replaceText(builder -> builder.matchLiteral("{player}").replacement(event.getPlayer().name()))
                            .replaceText(builder -> builder.matchLiteral("{playerCount}").replacement(String.valueOf(players.size())))
                            .replaceText(builder -> builder.matchLiteral("{maxPlayers}").replacement(String.valueOf(MAX_PLAYER_COUNT))));
                    setupPlayer(event.getPlayer());
                })
                .build());
    }

    @Override
    protected void onStateStart() {
        registerEvents();

        SPAWN_LOCATION.getWorld().setSpawnLocation(SPAWN_LOCATION);
        players.forEach(((uuid, player) -> setupPlayer(player.player())));

        super.onStateStart();
    }

    private void setupPlayer(Player player) {
        player.setGameMode(GameMode.ADVENTURE);

        player.setLevel(0);
        player.setExp(0);
        player.setHealth(20);
        player.setFoodLevel(20);

        Inventory inventory = player.getInventory();
        inventory.clear();
        givePlayerItems(inventory);

        player.teleport(SPAWN_LOCATION);
    }

    private void givePlayerItems(Inventory inventory) {
        inventory.setItem(0, LOADOUT_ITEM.buildItemStack());
        inventory.setItem(4, voteMapItem.buildItemStack());
    }

    @Override
    protected void onStateEnd() {
        super.onStateEnd();
        eventNode.unregisterAllListeners();

        GameMap gameMap = getVotedMap();
        FreeForAllActiveGameMap activeGameMap = loadGameMap(gameMap);

        game.switchGameState(new ActiveGameState(game, players, activeGameMap));
    }

    private FreeForAllActiveGameMap loadGameMap(GameMap gameMap) {
        String worldName = "game_world_" + game.getId();

        try {
            FileUtils.copyDirectory(gameMap.worldFolder(), new File(Bukkit.getWorldContainer().getParentFile(), worldName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        World world = Bukkit.createWorld(new WorldCreator(worldName)
                .generator(new VoidGenerator()));

        if (world == null) {
            throw new RuntimeException("Failed to load world: " + worldName);
        }

        world.setAutoSave(false);

        return new FreeForAllActiveGameMap(world, gameMap.configuration());
    }

    private GameMap getVotedMap() {
        return mapVotes.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseThrow();
    }
}
