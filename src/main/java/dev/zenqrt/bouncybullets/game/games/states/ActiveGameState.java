package dev.zenqrt.bouncybullets.game.games.states;

import dev.zenqrt.bouncybullets.BouncyBullets;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletPlayer;
import dev.zenqrt.bouncybullets.game.games.Gun;
import dev.zenqrt.bouncybullets.game.games.Loadout;
import dev.zenqrt.bouncybullets.game.impl.PaperGameState;
import dev.zenqrt.bouncybullets.item.GameItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public final class ActiveGameState extends PaperGameState {

    private static final int GAME_TIME = 300; // 5 minutes

    private final BouncyBulletGame game;
    private final Map<UUID, BouncyBulletPlayer> players;

    public ActiveGameState(BouncyBulletGame game, Map<UUID, BouncyBulletPlayer> players) {
        this.game = game;
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

        new GameTimerTask(GAME_TIME).runTaskTimer(BouncyBullets.getInstance(), 0, 20);
    }

    private static void setupPlayer(Player player, Loadout loadout) {
        PlayerInventory inventory = player.getInventory();
        inventory.clear();

        inventory.setItem(0, loadout.gun().buildItemStack());
    }

    private final class GameTimerTask extends BukkitRunnable {

        private int timeLeft;

        GameTimerTask(int time) {
            this.timeLeft = time;
        }

        @Override
        public void run() {
            if (--timeLeft == 0) {
                this.cancel();
                game.switchGameState(new EndingGameState());
            }
        }
    }
}
