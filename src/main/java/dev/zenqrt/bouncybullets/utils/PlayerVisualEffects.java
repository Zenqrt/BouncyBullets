package dev.zenqrt.bouncybullets.utils;

import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDistancePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.border.WorldBorder;
import org.bukkit.entity.Player;

public final class PlayerVisualEffects {

    private PlayerVisualEffects() {}

    public static void showLowHealthEffect(Player player) {
        WorldBorder border = new WorldBorder();
        border.setWarningBlocks(Integer.MAX_VALUE);

        ClientboundSetBorderWarningDistancePacket packet = new ClientboundSetBorderWarningDistancePacket(border);
        NMSConverter.serverPlayer(player).connection.send(packet);
    }

    public static void hideLowHealthEffect(Player player) {
        ServerPlayer nmsPlayer = NMSConverter.serverPlayer(player);

        ClientboundSetBorderWarningDistancePacket packet = new ClientboundSetBorderWarningDistancePacket(nmsPlayer.level().getWorldBorder());
        nmsPlayer.connection.send(packet);
    }

}
