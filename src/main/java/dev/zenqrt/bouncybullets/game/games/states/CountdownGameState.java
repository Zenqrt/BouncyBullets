package dev.zenqrt.bouncybullets.game.games.states;

import dev.zenqrt.bouncybullets.BouncyBullets;
import dev.zenqrt.bouncybullets.event.PlayerQuitGameEvent;
import dev.zenqrt.bouncybullets.game.EventGameState;
import dev.zenqrt.bouncybullets.game.event.impl.PaperEventListener;
import dev.zenqrt.bouncybullets.player.GamePlayerList;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.scheduler.BukkitRunnable;

public final class CountdownGameState extends EventGameState {

    private final PregameGameState pregameState;
    private final GamePlayerList players;
    private final int minPlayerCount;
    private final CountdownTask countdownTask;

    public CountdownGameState(PregameGameState pregameState, GamePlayerList players, int minPlayerCount) {
        this.pregameState = pregameState;
        this.players = players;
        this.minPlayerCount = minPlayerCount;
        this.countdownTask = new CountdownTask(15);
    }

    @Override
    public void registerEvents() {
        this.eventNode.registerListener(PaperEventListener.builder(PlayerQuitGameEvent.class)
                .filter(event -> event.getGame().getId() == pregameState.game.getId())
                .filter(event -> players.size() < minPlayerCount)
                .handler(event -> pregameState.switchPreviousState())
                .build());
    }

    @Override
    protected void onStateStart() {
        super.onStateStart();
        countdownTask.runTaskTimer(BouncyBullets.getInstance(), 0, 20);
    }

    @Override
    protected void onStateEnd() {
        countdownTask.cancel();
    }

    private class CountdownTask extends BukkitRunnable {

        private int timeLeft;

        CountdownTask(int countdownTime) {
            this.timeLeft = countdownTime;
        }

        @Override
        public void run() {
            if (timeLeft == 0) {
                pregameState.switchNextState();
                return;
            }

            if (timeLeft <= 5 || timeLeft % 15 == 0) {
                broadcastTimer();
            }

            timeLeft--;
        }

        private void broadcastTimer() {
            players.sendMessage(MiniMessage.miniMessage().deserialize("The game starts in <yellow>" + timeLeft + "</yellow> seconds!"));
            players.playSound(Sound.sound(org.bukkit.Sound.BLOCK_NOTE_BLOCK_HAT, Sound.Source.MASTER, 1, 1), Sound.Emitter.self());
        }
    }
}
