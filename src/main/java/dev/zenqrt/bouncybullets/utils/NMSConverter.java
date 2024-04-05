package dev.zenqrt.bouncybullets.utils;

import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public final class NMSConverter {

    public static ServerPlayer serverPlayer(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

}
