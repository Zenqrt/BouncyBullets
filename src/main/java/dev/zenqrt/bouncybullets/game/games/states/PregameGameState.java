package dev.zenqrt.bouncybullets.game.games.states;

import dev.zenqrt.bouncybullets.BouncyBullets;
import dev.zenqrt.bouncybullets.event.PlayerJoinGameEvent;
import dev.zenqrt.bouncybullets.game.event.impl.PaperEventListener;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.impl.PaperGameState;
import dev.zenqrt.bouncybullets.item.GameItem;
import dev.zenqrt.bouncybullets.item.items.LoadoutGameItem;
import dev.zenqrt.bouncybullets.item.items.VoteMapGameItem;
import dev.zenqrt.bouncybullets.map.FreeForAllGameMap;
import dev.zenqrt.bouncybullets.player.GamePlayerList;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.List;

public final class PregameGameState extends PaperGameState {

    private static final File MAPS_FOLDER = new File(BouncyBullets.getInstance().getDataFolder(), "maps");
    private static final int MIN_PLAYER_COUNT = 2;
    private static final Location SPAWN_LOCATION = new Location(Bukkit.getWorld("world"), 213.5, 65, 84.5);
    private static final LoadoutGameItem LOADOUT_ITEM = new LoadoutGameItem();
    private static final VoteMapGameItem VOTE_MAP_ITEM = new VoteMapGameItem();

    private final List<PaperGameState> states;
    private final GamePlayerList players;
    private final FreeForAllGameMap gameMap;
    private int currentStateIndex = 0;
    final BouncyBulletGame game;
    private PaperGameState currentState;

    public PregameGameState(BouncyBulletGame game, GamePlayerList players) {
        this.game = game;
        this.players = players;
        this.gameMap = loadGameMap("test");

        WaitingGameState waitingState = new WaitingGameState(this, players, MIN_PLAYER_COUNT);
        this.states = List.of(waitingState, new CountdownGameState(this, players, MIN_PLAYER_COUNT), new SetupMapGameState(this, this.game.getId(), gameMap));
        this.currentState = states.get(0);
    }

    private static FreeForAllGameMap loadGameMap(String mapName) {
        File gameMapFolder = new File(MAPS_FOLDER, mapName);
        File worldFolder = new File(gameMapFolder, "world");

        return new FreeForAllGameMap(gameMapFolder, worldFolder, List.of(new Vector(17, -10, 32)), List.of());
    }

    void switchNextState() {
        if (currentStateIndex + 1 >= states.size()) {
            game.switchGameState(new ActiveGameState(game, players, gameMap));
            return;
        }

        currentState.end();

        currentState = states.get(++currentStateIndex);
        currentState.start();
    }

    void switchPreviousState() {
        currentState.end();

        currentState = states.get(--currentStateIndex);
        currentState.start();
    }

    @Override
    public void registerEvents() {
        GameItem.registerGameItemEvents(this.eventNode, List.of(LOADOUT_ITEM, VOTE_MAP_ITEM));

        this.eventNode.registerListener(PaperEventListener.builder(PlayerDropItemEvent.class)
                .handler(event -> event.setCancelled(true))
                .build());
        this.eventNode.registerListener(PaperEventListener.builder(PlayerJoinGameEvent.class)
                .filter(event -> event.getGame().getId() == game.getId())
                .handler(event -> setupPlayer(event.getPlayer()))
                .build());
    }

    @Override
    protected void onStateStart() {
        SPAWN_LOCATION.getWorld().setSpawnLocation(SPAWN_LOCATION);

        players.forEach(((uuid, player) -> setupPlayer(player.player())));
        currentState.start();
    }

    private static void setupPlayer(Player player) {
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

    private static void givePlayerItems(Inventory inventory) {
        inventory.setItem(0, LOADOUT_ITEM.buildItemStack());
        inventory.setItem(4, VOTE_MAP_ITEM.buildItemStack());
    }

}
