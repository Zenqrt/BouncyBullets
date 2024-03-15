package dev.zenqrt.bouncybullets.game.games.states;

import dev.zenqrt.bouncybullets.game.games.BouncyBulletPlayer;
import dev.zenqrt.bouncybullets.game.games.Gun;
import dev.zenqrt.bouncybullets.game.games.Loadout;
import dev.zenqrt.bouncybullets.game.impl.PaperGameState;
import dev.zenqrt.bouncybullets.item.GameItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public final class ActiveGameState extends PaperGameState {

    private final Map<UUID, BouncyBulletPlayer> players;

    public ActiveGameState(Map<UUID, BouncyBulletPlayer> players) {
        this.players = players;
    }

    @Override
    public void registerEvents() {
        GameItem.registerGameItemEvents(eventNode, Stream.of(Gun.values())
                .map(gun -> (GameItem) gun.getItem())
                .toList()
        );
    }

    @Override
    protected void onStateStart() {
        players.forEach((uuid, player) -> setupPlayer(player.player(), player.loadout()));
    }

    private static void setupPlayer(Player player, Loadout loadout) {
        PlayerInventory inventory = player.getInventory();
        inventory.clear();

        inventory.setItem(0, loadout.gun().buildItemStack());
    }
}
