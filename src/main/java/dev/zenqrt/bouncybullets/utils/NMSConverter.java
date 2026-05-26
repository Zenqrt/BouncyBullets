package dev.zenqrt.bouncybullets.utils;

import io.papermc.paper.adventure.PaperAdventure;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

public final class NMSConverter {

    public static ServerPlayer serverPlayer(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    public static Component component(net.kyori.adventure.text.Component adventureComponent) {
        return PaperAdventure.asVanilla(adventureComponent);
    }

}
