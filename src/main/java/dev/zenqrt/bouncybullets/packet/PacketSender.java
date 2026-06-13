package dev.zenqrt.bouncybullets.packet;

import com.mojang.logging.LogUtils;
import io.netty.channel.ChannelFuture;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

public final class PacketSender {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static void sendNow(ServerPlayer player, Packet<?> packet) {
        packet.onPacketDispatch(player);

        Connection connection = player.connection.connection;

        if (!connection.isConnected()) {
            packet.onPacketDispatchFinish(player, null);
            return;
        }

        try {
            ChannelFuture future = connection.channel.writeAndFlush(packet, connection.channel.voidPromise());

            if (packet.hasFinishListener())
                packet.onPacketDispatchFinish(player, future);
        } catch (Exception ex) {
            LOGGER.error("NetworkException: {}", player, ex);

            Component reason = Component.translatable("disconnect.genericReason", "Internal Exception: " + ex.getMessage());
            connection.send(
                    new ClientboundDisconnectPacket(reason),
                    PacketSendListener.thenRun(() -> connection.disconnect(reason))
            );

            packet.onPacketDispatchFinish(player, null);
        }
    }

    private PacketSender() {
    }

}
