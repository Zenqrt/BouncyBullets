package dev.zenqrt.bouncybullets.utils.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;

import java.util.ArrayList;
import java.util.List;

public final class EntityUtils {

    private EntityUtils() {}

    public static List<SynchedEntityData.DataValue<?>> createValuesWithLivingEntityFlag(byte flagId, List<SynchedEntityData.DataValue<?>> dataValues) {
        List<SynchedEntityData.DataValue<?>> newDataValues = new ArrayList<>(dataValues);

        EntityDataAccessor<Byte> accessor = LivingEntityAccessor.DATA_LIVING_ENTITY_FLAGS;
        var serializer = accessor.serializer();
        int dataSharedFlagsId = accessor.id();

        for (int i = 0; i < dataValues.size(); i++) {
            SynchedEntityData.DataValue<?> value = dataValues.get(i);

            if (value.id() != dataSharedFlagsId)
                continue;

            byte flags = (byte) value.value();

            newDataValues.set(i, new SynchedEntityData.DataValue<>(dataSharedFlagsId, serializer, (byte) (flags | flagId)));
            return newDataValues;
        }

        newDataValues.add(new SynchedEntityData.DataValue<>(dataSharedFlagsId, serializer, flagId));
        return newDataValues;
    }

    public static List<SynchedEntityData.DataValue<?>> createValuesWithoutLivingEntityFlag(byte flagId, List<SynchedEntityData.DataValue<?>> dataValues) {
        List<SynchedEntityData.DataValue<?>> newDataValues = new ArrayList<>(dataValues);

        EntityDataAccessor<Byte> accessor = LivingEntityAccessor.DATA_LIVING_ENTITY_FLAGS;
        var serializer = accessor.serializer();
        int dataSharedFlagsId = accessor.id();

        for (int i = 0; i < dataValues.size(); i++) {
            SynchedEntityData.DataValue<?> value = dataValues.get(i);

            if (value.id() != dataSharedFlagsId)
                continue;

            byte flags = (byte) value.value();

            newDataValues.set(i, new SynchedEntityData.DataValue<>(dataSharedFlagsId, serializer, (byte) (flags ^ flagId)));
            return newDataValues;
        }

        newDataValues.add(new SynchedEntityData.DataValue<>(dataSharedFlagsId, serializer, (byte) 0));
        return newDataValues;
    }

}
