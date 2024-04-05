package dev.zenqrt.bouncybullets.utils;

import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class GlowUtils {

    public static void showGlow(Player player, Player target) {
        if (target.isGlowing())
            return;

        List<SynchedEntityData.DataValue<?>> dataValues = addGlowToDataValues(Objects.requireNonNullElse(NMSConverter.serverPlayer(target).getEntityData().packDirty(), new ArrayList<>()));

        ClientboundSetEntityDataPacket packet = new ClientboundSetEntityDataPacket(player.getEntityId(), dataValues);
        NMSConverter.serverPlayer(player).connection.send(packet);
    }

    public static List<SynchedEntityData.DataValue<?>> addGlowToDataValues(List<SynchedEntityData.DataValue<?>> dataValues) {
        EntityDataAccessor<Byte> accessor = ReflectionUtils.getStaticDeclaredField(Entity.class, "ao");
        var serializer = accessor.getSerializer();
        int dataSharedFlagsId = accessor.getId();

        SynchedEntityData.DataValue<Byte> dataValue = dataValues.stream()
                .filter(data -> data.id() == dataSharedFlagsId)
                .findFirst()
                .map(value -> {
                    dataValues.remove(value);

                    return new SynchedEntityData.DataValue<>(dataSharedFlagsId, serializer, (byte) ((byte) value.value() | 1 << 6));
                })
                .orElseGet(() -> new SynchedEntityData.DataValue<>(dataSharedFlagsId, serializer, (byte) (1 << 6)));

        dataValues.add(dataValue);

        return dataValues;
    }

}
