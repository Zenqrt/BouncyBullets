package dev.zenqrt.bouncybullets.command.commands;

import dev.zenqrt.bouncybullets.packet.PacketSender;
import dev.zenqrt.bouncybullets.utils.NMSConverter;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Named;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static dev.zenqrt.bouncybullets.item.items.guns.GunItem.createHeadRotationPacket;

public final class RecoilCommand {

    private final Map<UUID, ScheduledTask> tasks = new HashMap<>();
    private final Plugin plugin;

    public RecoilCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Command("recoil")
    public void onRecoil(
            Player player,
            @Named("pitch_amp") float pitchAmplitude,
            @Named("yaw_amp") float yawAmplitude,
            @Named("period_ms") int periodMs
    ) {
        ScheduledTask existingTask = this.tasks.remove(player.getUniqueId());

        if (existingTask != null)
            existingTask.cancel();

        float cycles = 2;
        long startTime = System.currentTimeMillis();
        ServerPlayer nmsPlayer = NMSConverter.serverPlayer(player);

        AtomicReference<Float> xRotAngle = new AtomicReference<>(0F);
        AtomicReference<Float> yRotAngle = new AtomicReference<>(0F);

//        ClientboundPlayerPositionPacket startPacket = createHeadRotationPacket();

//        PacketSender.sendNow(nmsPlayer, startPacket);
//        nmsPlayer.connection.send(startPacket);

        /*
        - Calculate angle with decaying exponential sine
        - Get difference from that and xRotAngle
        - Save current angle to xRotAngle
        - Apply difference to player
         */
        ScheduledTask animationTask = Bukkit.getAsyncScheduler().runAtFixedRate(
                plugin,
                task -> {
                    float elapsed = (System.currentTimeMillis() - startTime) / 1000f;

                    if (elapsed >= 4 * Math.PI * cycles - 1) {
                        task.cancel();
                        return;
                    }

                    float shakeXRot;
                    float cutoff = 0.1F;


                    if (elapsed > cutoff) {
                        shakeXRot = (float) -(Math.exp(-2 * elapsed)
                                * Math.sin(Math.PI * (elapsed - cutoff))
                                * 3
                        );
                    } else {
                        shakeXRot = (float) (pitchAmplitude * Math.sin(Math.PI * elapsed / cutoff));
                    }
                    float shakeYRot = (float) (Math.exp(-5 * elapsed)
                            * Math.cos(elapsed)
                            * yawAmplitude
                    );

                    float deltaXRot = shakeXRot - xRotAngle.getAndSet(shakeXRot);
                    float deltaYRot = shakeYRot - yRotAngle.getAndSet(shakeYRot);

                    ClientboundPlayerPositionPacket positionPacket = createHeadRotationPacket(-deltaXRot, deltaYRot);

                    PacketSender.sendNow(nmsPlayer, positionPacket);
//                    nmsPlayer.connection.send(positionPacket);
                },
                0,
                periodMs,
                TimeUnit.MILLISECONDS
        );

        this.tasks.put(player.getUniqueId(), animationTask);
        player.sendMessage("Playing...");
    }

}
