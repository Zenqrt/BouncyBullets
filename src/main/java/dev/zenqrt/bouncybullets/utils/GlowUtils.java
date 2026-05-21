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

    public static void showGlow(Entity entity, Player viewer) {
        List<SynchedEntityData.DataValue<?>> dataValues = Objects.requireNonNullElse(
                entity.getEntityData().getNonDefaultValues(),
                new ArrayList<>()
        );

        ClientboundSetEntityDataPacket dataPacket = new ClientboundSetEntityDataPacket(
                entity.getId(),
                createDataValuesWithGlow(dataValues)
        );

        NMSConverter.serverPlayer(viewer).connection.send(dataPacket);
    }

    public static List<SynchedEntityData.DataValue<?>> createDataValuesWithGlow(List<SynchedEntityData.DataValue<?>> dataValues) {
        List<SynchedEntityData.DataValue<?>> newDataValues = new ArrayList<>(dataValues);

        EntityDataAccessor<Byte> accessor = ReflectionUtils.getStaticDeclaredField(Entity.class, "ao");
        var serializer = accessor.getSerializer();
        int dataSharedFlagsId = accessor.getId();

        for (int i = 0; i < dataValues.size(); i++) {
            SynchedEntityData.DataValue<?> value = dataValues.get(i);

            if (value.id() != dataSharedFlagsId)
                continue;

            byte flags = (byte) value.value();

            newDataValues.set(i, new SynchedEntityData.DataValue<>(dataSharedFlagsId, serializer, (byte) (flags | (1 << 6))));
            return newDataValues;
        }

        newDataValues.add(new SynchedEntityData.DataValue<>(dataSharedFlagsId, serializer, (byte) (1 << 6)));
        return newDataValues;
    }

    public static List<SynchedEntityData.DataValue<?>> createDataValuesWithoutGlow(List<SynchedEntityData.DataValue<?>> dataValues) {
        List<SynchedEntityData.DataValue<?>> newDataValues = new ArrayList<>(dataValues);

        EntityDataAccessor<Byte> accessor = ReflectionUtils.getStaticDeclaredField(Entity.class, "ao");
        var serializer = accessor.getSerializer();
        int dataSharedFlagsId = accessor.getId();

        for (int i = 0; i < dataValues.size(); i++) {
            SynchedEntityData.DataValue<?> value = dataValues.get(i);

            if (value.id() != dataSharedFlagsId)
                continue;

            byte flags = (byte) value.value();

            newDataValues.set(i, new SynchedEntityData.DataValue<>(dataSharedFlagsId, serializer, (byte) (flags ^ (1 << 6))));
            return newDataValues;
        }

        newDataValues.add(new SynchedEntityData.DataValue<>(dataSharedFlagsId, serializer, (byte) 0));
        return newDataValues;
    }

}
