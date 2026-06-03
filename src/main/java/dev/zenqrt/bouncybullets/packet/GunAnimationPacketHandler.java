package dev.zenqrt.bouncybullets.packet;

import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import dev.zenqrt.bouncybullets.utils.entity.EntityUtils;
import dev.zenqrt.bouncybullets.utils.entity.LivingEntityFlags;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;

import java.util.List;

public final class GunAnimationPacketHandler extends ChannelDuplexHandler {

    private final Int2ObjectMap<BouncyBulletGamePlayer> idToGamePlayerMap;
    private final int selfEntityId;

    public GunAnimationPacketHandler(int selfEntityId, Int2ObjectMap<BouncyBulletGamePlayer> idToGamePlayerMap) {
        this.selfEntityId = selfEntityId;
        this.idToGamePlayerMap = idToGamePlayerMap;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof ClientboundSetEntityDataPacket(int id, List<SynchedEntityData.DataValue<?>> packedItems)) {
            if (this.selfEntityId != id) {
                BouncyBulletGamePlayer gamePlayer = this.idToGamePlayerMap.get(id);

                if (gamePlayer != null && gamePlayer.isAiming()) {
                    ClientboundSetEntityDataPacket modifiedPacket = new ClientboundSetEntityDataPacket(
                            id,
                            EntityUtils.createValuesWithLivingEntityFlag(
                                    LivingEntityFlags.LIVING_ENTITY_FLAG_IS_USING,
                                    packedItems
                            )
                    );

                    super.write(ctx, modifiedPacket, promise);
                    return;
                }
            }
        }

        super.write(ctx, msg, promise);
    }
}
