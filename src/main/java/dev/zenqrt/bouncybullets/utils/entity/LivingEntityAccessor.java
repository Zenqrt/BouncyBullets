package dev.zenqrt.bouncybullets.utils.entity;

import dev.zenqrt.bouncybullets.utils.ReflectionUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.LivingEntity;

public final class LivingEntityAccessor {

    public static final EntityDataAccessor<Byte> DATA_LIVING_ENTITY_FLAGS = ReflectionUtils.getStaticDeclaredField(LivingEntity.class, "DATA_LIVING_ENTITY_FLAGS");

    private LivingEntityAccessor() {}

}
