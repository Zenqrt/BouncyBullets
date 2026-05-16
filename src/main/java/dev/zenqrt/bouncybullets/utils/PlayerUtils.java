package dev.zenqrt.bouncybullets.utils;

import net.minecraft.world.entity.Entity;
import org.bukkit.entity.Player;

public final class PlayerUtils {

    private PlayerUtils() {
    }

    public static void forceRemove(Player player) {
        NMSConverter.serverPlayer(player).remove(Entity.RemovalReason.DISCARDED);
    }

}
