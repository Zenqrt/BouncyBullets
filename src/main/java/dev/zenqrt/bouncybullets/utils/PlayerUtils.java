package dev.zenqrt.bouncybullets.utils;

import io.netty.channel.ChannelHandler;
import net.minecraft.network.Connection;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.world.entity.Entity;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class PlayerUtils {

    private PlayerUtils() {
    }

    public static void forceRemove(Player player) {
        NMSConverter.serverPlayer(player).remove(Entity.RemovalReason.DISCARDED);
    }

    public static void injectPacketListener(Player player, String id, ChannelHandler handler) {
        Connection connection = ReflectionUtils.getDeclaredField(ServerCommonPacketListenerImpl.class, NMSConverter.serverPlayer(player).connection, "c");
        connection.channel.pipeline().addBefore("packet_handler", id, handler);
    }

    public static void removePacketListener(Player player, ChannelHandler handler) {
        Connection connection = ReflectionUtils.getDeclaredField(ServerCommonPacketListenerImpl.class, NMSConverter.serverPlayer(player).connection, "c");
        connection.channel.pipeline().remove(handler);
    }

    public static @NotNull AttributeInstance requireNonNullAttribute(Player player, Attribute attribute) {
        return Objects.requireNonNull(
                player.getAttribute(attribute),
                attribute::name
        );
    }
}
