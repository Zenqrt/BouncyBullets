package dev.zenqrt.bouncybullets.event.listeners;

import dev.zenqrt.bouncybullets.glow.GlowManager;
import dev.zenqrt.bouncybullets.utils.GlowUtils;
import dev.zenqrt.bouncybullets.utils.NMSConverter;
import dev.zenqrt.bouncybullets.utils.ReflectionUtils;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class GlowListeners implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Connection connection = ReflectionUtils.getDeclaredField(ServerCommonPacketListenerImpl.class, NMSConverter.serverPlayer(player).connection, "c");
        connection.channel.pipeline()
                .addBefore("packet_handler", "glow", new ChannelDuplexHandler() {

                    @Override
                    public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
                        if (packet instanceof ClientboundSetEntityDataPacket dataPacket && GlowManager.isGlowing(player.getUniqueId(), dataPacket.id())) {
                            ClientboundSetEntityDataPacket newPacket = new ClientboundSetEntityDataPacket(dataPacket.id(), GlowUtils.addGlowToDataValues(dataPacket.packedItems()));
                            super.write(ctx, newPacket, promise);
                            return;
                        }

                        super.write(ctx, packet, promise);
                    }
                });
    }

}
