package dev.zenqrt.bouncybullets.game.games.states;

import dev.zenqrt.bouncybullets.event.PlayerJoinGameEvent;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletPlayer;
import dev.zenqrt.bouncybullets.game.impl.PaperGameEventHandler;
import dev.zenqrt.bouncybullets.game.impl.PaperGameState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

public final class WaitingGameState extends PaperGameState {

    private static final Location SPAWN_LOCATION = new Location(Bukkit.getWorld("world"), 0, 0, 0);

    private final int gameId;
    private final Map<UUID, BouncyBulletPlayer> players;

    public WaitingGameState(PaperGameEventHandler eventHandler, int gameId, Map<UUID, BouncyBulletPlayer> players) {
        super(eventHandler);

        this.gameId = gameId;
        this.players = players;
    }

    @Override
    public void registerEvents() {
        this.eventHandler.registerEvent(PlayerJoinGameEvent.class, event -> {
            if (event.getGame().getId() == gameId) {
                Player player = event.getPlayer();

                player.teleport(SPAWN_LOCATION);
                player.setGameMode(GameMode.ADVENTURE);
                givePlayerItems(player);
            }
        });
    }

    @Override
    protected void onStateStart() {
        SPAWN_LOCATION.getWorld().setSpawnLocation(SPAWN_LOCATION);

        players.forEach(((uuid, player) -> {
            player.player().teleport(SPAWN_LOCATION);
            givePlayerItems(player.player());
        }));
    }

    private static void givePlayerItems(Player player) {
        ItemStack loadoutItem = new ItemStack(Material.NETHER_STAR);
        loadoutItem.editMeta(meta -> meta.displayName(Component.text("Loadout", NamedTextColor.YELLOW)));

        ItemStack votingItem = new ItemStack(Material.BELL);
        votingItem.editMeta(meta -> meta.displayName(Component.text("Vote Map", NamedTextColor.YELLOW)));

        Inventory inventory = player.getInventory();
        inventory.setItem(0, loadoutItem);
        inventory.setItem(4, votingItem);
    }


}
