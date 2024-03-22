package dev.zenqrt.bouncybullets.game.games.states;

import dev.zenqrt.bouncybullets.BouncyBullets;
import dev.zenqrt.bouncybullets.event.PlayerQuitGameEvent;
import dev.zenqrt.bouncybullets.game.event.impl.PaperEventListener;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletPlayer;
import dev.zenqrt.bouncybullets.game.impl.PaperGameState;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;

public final class CountdownGameState extends PaperGameState {

    private final PregameGameState pregameState;
    private final Map<UUID, BouncyBulletPlayer> players;
    private final int minPlayerCount;
    private final CountdownTask countdownTask;

    public CountdownGameState(PregameGameState pregameState, Map<UUID, BouncyBulletPlayer> players, int minPlayerCount) {
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
                .handler(event -> {
                    countdownTask.cancel();
                    pregameState.switchPreviousState();
                })
                .build());
    }

    @Override
    protected void onStateStart() {
        countdownTask.runTaskTimer(BouncyBullets.getInstance(), 0, 20);
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
                this.cancel();
                return;
            }

            if (timeLeft <= 5 || timeLeft % 15 == 0) {
                broadcastTimer();
            }

            timeLeft--;
        }

        private void broadcastTimer() {
            players.forEach(((uuid, player) -> {
                player.player().sendMessage(
                    MiniMessage.miniMessage().deserialize("The game starts in <yellow>" + timeLeft + "</yellow> seconds!"));
                player.player().playSound(Sound.sound(org.bukkit.Sound.BLOCK_NOTE_BLOCK_HAT, Sound.Source.MASTER, 1, 1), Sound.Emitter.self());
            }));
        }
    }
}
