package dev.zenqrt.bouncybullets.game.games.states;

import dev.zenqrt.bouncybullets.BouncyBullets;
import dev.zenqrt.bouncybullets.game.event.impl.PaperEventListener;
import dev.zenqrt.bouncybullets.game.event.impl.PaperEventNode;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletPlayer;
import dev.zenqrt.bouncybullets.game.impl.PaperGameState;
import org.bukkit.event.Event;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;


// NOTE: Players should not be passed in here. Try figuring out another way to transition states that isn't in the state classes.
public final class CountdownGameState extends PaperGameState {

    private final BouncyBulletGame game;
    private final Map<UUID, BouncyBulletPlayer> players;
    private final WaitingGameState fallbackState;

    public CountdownGameState(BouncyBulletGame game, Map<UUID, BouncyBulletPlayer> players, WaitingGameState fallbackState) {
        this.game = game;
        this.players = players;
        this.fallbackState = fallbackState;
    }

    @Override
    public void registerEvents() {
        this.eventNode.registerListener(PaperEventListener.builder());
    }

    @Override
    protected void onStateStart() {
        new CountdownTask(15)
                .runTaskTimer(BouncyBullets.getInstance(), 0, 20);
    }

    private class CountdownTask extends BukkitRunnable {

        private int timeLeft;

        CountdownTask(int countdownTime) {
            this.timeLeft = countdownTime;
        }

        @Override
        public void run() {
            if (timeLeft == 0) {
                game.switchGameState(new ActiveGameState(players));
                this.cancel();
            }

            timeLeft--;
        }
    }
}
