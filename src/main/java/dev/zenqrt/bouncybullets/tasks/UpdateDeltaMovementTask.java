package dev.zenqrt.bouncybullets.tasks;

import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import dev.zenqrt.bouncybullets.player.PlayerSessionManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class UpdateDeltaMovementTask implements Runnable {

    private final Map<UUID, PoseStamped> lastPositionMap = new HashMap<>();
    private final PlayerSessionManager sessionManager;

    public UpdateDeltaMovementTask(PlayerSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            Optional<BouncyBulletGame> gameOptional = this.sessionManager.findGameSession(uuid);

            if (gameOptional.isEmpty()) {
                this.lastPositionMap.remove(uuid);
                continue;
            }

            Location location = player.getLocation();
            PoseStamped currentPosition = new PoseStamped(
                    location.getWorld().getGameTime(),
                    location.getX(),
                    location.getY(),
                    location.getZ(),
                    location.getYaw(),
                    location.getPitch()
            );
            PoseStamped lastPosition = this.lastPositionMap.get(uuid);

            this.lastPositionMap.put(uuid, currentPosition);

            if (lastPosition == null) {
                continue;
            }

            long deltaTime = currentPosition.time() - lastPosition.time();

            BouncyBulletGamePlayer gamePlayer = gameOptional.get().findPlayerOrThrow(uuid);
            gamePlayer.setDeltaMovement(
                    new Vector(
                            (currentPosition.x() - lastPosition.x()) / deltaTime,
                            (currentPosition.y() - lastPosition.y()) / deltaTime,
                            (currentPosition.z() - lastPosition.z()) / deltaTime
                    )
            );
            gamePlayer.setDeltaYaw(
                    (currentPosition.yaw() - lastPosition.yaw()) / deltaTime
            );
            gamePlayer.setDeltaPitch(
                    (currentPosition.pitch() - lastPosition.pitch()) / deltaTime
            );
        }
    }

    private record PoseStamped(long time, double x, double y, double z, float yaw, float pitch) {}
}
