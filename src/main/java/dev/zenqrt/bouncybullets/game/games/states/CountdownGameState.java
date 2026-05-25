package dev.zenqrt.bouncybullets.game.games.states;

import dev.zenqrt.bouncybullets.BouncyBulletsPlugin;
import dev.zenqrt.bouncybullets.event.EventNode;
import dev.zenqrt.bouncybullets.event.GameEventNodes;
import dev.zenqrt.bouncybullets.event.PaperEventListener;
import dev.zenqrt.bouncybullets.event.events.PlayerQuitGameEvent;
import dev.zenqrt.bouncybullets.game.base.GameState;
import dev.zenqrt.bouncybullets.player.GamePlayerList;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.scheduler.BukkitRunnable;

public final class CountdownGameState extends GameState {

    private final EventNode<PlayerEvent> playerEventNode;

    private final PregameGameState pregameState;
    private final GamePlayerList players;
    private final int minPlayerCount;
    private final CountdownTask countdownTask;

    public CountdownGameState(PregameGameState pregameState, GamePlayerList players, int minPlayerCount) {
        this.pregameState = pregameState;
        this.players = players;
        this.minPlayerCount = minPlayerCount;
        this.countdownTask = new CountdownTask(15);

        this.playerEventNode = GameEventNodes.filteredPlayerEvents(pregameState.game);
    }

    @Override
    protected void onStateStart() {
        super.onStateStart();

        registerEvents();
        this.countdownTask.runTaskTimer(BouncyBulletsPlugin.getInstance(), 0, 20);
    }

    private void registerEvents() {
        this.playerEventNode.registerListener(PaperEventListener.builder(PlayerQuitGameEvent.class)
                .filter(event -> event.getGame().getId() == this.pregameState.game.getId())
                .filter(_ -> this.players.size() < this.minPlayerCount)
                .handler(_ -> this.pregameState.switchPreviousState())
                .build());
    }

    @Override
    protected void onStateEnd() {
        this.countdownTask.cancel();
        this.playerEventNode.unregisterAllListeners();
    }

    private class CountdownTask extends BukkitRunnable {

        private int timeLeft;

        CountdownTask(int countdownTime) {
            this.timeLeft = countdownTime;
        }

        @Override
        public void run() {
            if (this.timeLeft == 0) {
                CountdownGameState.this.pregameState.switchNextState();
                return;
            }

            if (this.timeLeft <= 5 || this.timeLeft % 15 == 0) {
                broadcastTimer();
            }

            this.timeLeft--;
        }

        private void broadcastTimer() {
            CountdownGameState.this.players.sendMessage(MiniMessage.miniMessage().deserialize("The game starts in <yellow>" + this.timeLeft + "</yellow> seconds!"));
            CountdownGameState.this.players.playSound(Sound.sound(org.bukkit.Sound.BLOCK_NOTE_BLOCK_HAT, Sound.Source.MASTER, 1, 1), Sound.Emitter.self());
        }
    }
}
