package dev.zenqrt.bouncybullets.game.games.states;

import dev.zenqrt.bouncybullets.event.PlayerJoinGameEvent;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletPlayer;
import dev.zenqrt.bouncybullets.game.impl.PaperGameEventHandler;
import dev.zenqrt.bouncybullets.game.impl.PaperGameState;
import dev.zenqrt.bouncybullets.item.GameItem;
import dev.zenqrt.bouncybullets.item.items.LoadoutGameItem;
import dev.zenqrt.bouncybullets.item.items.VoteMapGameItem;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.UUID;

public final class WaitingGameState extends PaperGameState {

    private static final Location SPAWN_LOCATION = new Location(Bukkit.getWorld("world"), 213.5, 65, 84.5);
    private static final LoadoutGameItem LOADOUT_ITEM = new LoadoutGameItem();
    private static final VoteMapGameItem VOTE_MAP_ITEM = new VoteMapGameItem();

    private final BouncyBulletGame game;
    private final Map<UUID, BouncyBulletPlayer> players;

    public WaitingGameState(PaperGameEventHandler eventHandler, BouncyBulletGame game, Map<UUID, BouncyBulletPlayer> players) {
        super(eventHandler);

        this.game = game;
        this.players = players;
    }

    @Override
    public void registerEvents() {
        GameItem.registerGameItemEvents(this.eventHandler, LOADOUT_ITEM, VOTE_MAP_ITEM);

        this.eventHandler.registerEvent(PlayerJoinGameEvent.class, event -> {
            if (event.getGame().getId() == game.getId()) {
                setupPlayer(event.getPlayer());

                game.switchGameState(new ActiveGameState(new PaperGameEventHandler(), players));
            }
        });
    }

    @Override
    protected void onStateStart() {
        SPAWN_LOCATION.getWorld().setSpawnLocation(SPAWN_LOCATION);

        players.forEach(((uuid, player) -> setupPlayer(player.player())));
    }

    private static void setupPlayer(Player player) {
        player.getInventory().clear();
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.teleport(SPAWN_LOCATION);
        givePlayerItems(player);
    }

    private static void givePlayerItems(Player player) {
        Inventory inventory = player.getInventory();
        inventory.setItem(0, LOADOUT_ITEM.buildItemStack());
        inventory.setItem(4, VOTE_MAP_ITEM.buildItemStack());
    }

}
