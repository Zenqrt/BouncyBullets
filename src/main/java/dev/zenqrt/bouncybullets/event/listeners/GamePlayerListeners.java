package dev.zenqrt.bouncybullets.event.listeners;

import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import dev.zenqrt.bouncybullets.player.PlayerSessionManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.Optional;

public final class GamePlayerListeners implements Listener {

    private final PlayerSessionManager sessionManager;

    public GamePlayerListeners(PlayerSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled())
            return;

        Player player = event.getPlayer();
        Optional<BouncyBulletGame> gameOptional = this.sessionManager.findGameSession(player.getUniqueId());

        if (gameOptional.isEmpty())
            return;

        BouncyBulletGamePlayer gamePlayer = gameOptional.get().findPlayerOrThrow(player.getUniqueId());
        Vector deltaMovement = event.getTo().clone()
                .subtract(event.getFrom())
                .toVector();
        Vector nudge = deltaMovement.clone()
                .normalize()
                .multiply(1);

        gamePlayer.setDeltaMovement(
                deltaMovement.add(nudge)
        );
    }

}
