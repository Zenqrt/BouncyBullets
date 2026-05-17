package dev.zenqrt.bouncybullets.utils;

import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public final class NMSConverter {

    public static ServerPlayer serverPlayer(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    public static Component component(net.kyori.adventure.text.Component adventureComponent) {
        String jsonString = GsonComponentSerializer.gson().serialize(adventureComponent);

        return Component.Serializer.fromJson(jsonString);
    }

}
