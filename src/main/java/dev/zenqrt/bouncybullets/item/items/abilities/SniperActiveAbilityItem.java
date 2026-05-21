package dev.zenqrt.bouncybullets.item.items.abilities;

import dev.zenqrt.bouncybullets.event.events.GunShootEvent;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import dev.zenqrt.bouncybullets.loadout.gun.BulletProperties;
import dev.zenqrt.bouncybullets.utils.GlowUtils;
import dev.zenqrt.bouncybullets.utils.MiniMessageUtils;
import dev.zenqrt.bouncybullets.utils.NMSConverter;
import dev.zenqrt.bouncybullets.utils.PlayerUtils;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public final class SniperActiveAbilityItem extends ActiveAbilityItem implements Listener {

    private static final int COOLDOWN_TICKS = 1200;  // 60 seconds
    private static final int DAMAGE_MULTIPLIER = 2;
    private static final int ABILITY_DURATION_TICKS = 200;      // 10 seconds
    private static final String GLOW_PACKET_LISTENER_ID = "sniper_active_ability_glow";
    private static final Sound ACTIVATE_SOUND = Sound.sound(Key.key("block.portal.travel"), Sound.Source.MASTER, 0.5F, 2);

    private final Set<UUID> abilityActive = new HashSet<>();

    public SniperActiveAbilityItem() {
        super(
                "sniper_active_ability",
                Material.SPECTRAL_ARROW,
                "Eye Spy",
                MiniMessageUtils.wordWrapLore(
                        List.of(
                                "<gray>Make all players glow for <green>" + (ABILITY_DURATION_TICKS / 20) + "s<gray>. Your next shot does <red>" + DAMAGE_MULTIPLIER + "x <gray>more damage.",
                                "",
                                "<dark_gray>Cooldown: <green>" + (COOLDOWN_TICKS / 20) + "s"
                        ),
                        30
                )
        );
    }

    @EventHandler
    public void onGunShoot(GunShootEvent event) {
        System.out.println("Gun shot by: " + event.getShooter().getUniqueId());
        if (!this.abilityActive.contains(event.getShooter().getUniqueId()))
            return;

        System.out.println("Yes");

        BulletProperties bulletProperties = event.getBulletProperties();
        BulletProperties modifiedProperties = bulletProperties
                .withMaxDamage(bulletProperties.maxDamage() * DAMAGE_MULTIPLIER);

        event.setBulletProperties(modifiedProperties);
    }

    @Override
    public void onUse(BouncyBulletGame game, Player player, ItemStack itemStack, PlayerInteractEvent event) {
        this.abilityActive.add(player.getUniqueId());
        System.out.println("Used: " + player.getUniqueId());

        IntSet ids = new IntOpenHashSet();

        for (BouncyBulletGamePlayer gamePlayer : game.getPlayers().values()) {
            if (gamePlayer.getUuid().equals(player.getUniqueId()))
                continue;

            Player otherPlayer = gamePlayer.getPlayer();

            ids.add(otherPlayer.getEntityId());
            GlowUtils.showGlow(NMSConverter.serverPlayer(otherPlayer), player);
        }

        ChannelHandler packetListener = new ModifyGlowDataPacketListener(ids);
        PlayerUtils.injectPacketListener(player, GLOW_PACKET_LISTENER_ID, packetListener);

        player.playSound(ACTIVATE_SOUND, Sound.Emitter.self());

        Bukkit.getScheduler().runTaskLater(
                game.getPlugin(),
                () -> {
                    PlayerUtils.removePacketListener(player, packetListener);
                    resetAllGlows(game, player);

                    this.abilityActive.remove(player.getUniqueId());
                },
                ABILITY_DURATION_TICKS
        );

        player.setCooldown(super.material, 200);
    }

    private static void resetAllGlows(BouncyBulletGame game, Player viewer) {
        List<Packet<ClientGamePacketListener>> dataPackets = new ArrayList<>();

        for (BouncyBulletGamePlayer gamePlayer : game.getPlayers().values()) {
            if (gamePlayer.getUuid().equals(viewer.getUniqueId()))
                continue;

            Player otherPlayer = gamePlayer.getPlayer();
            List<SynchedEntityData.DataValue<?>> dataValues = NMSConverter.serverPlayer(otherPlayer)
                    .getEntityData()
                    .getNonDefaultValues();

            if (dataValues == null)
                continue;

            dataPackets.add(
                    new ClientboundSetEntityDataPacket(
                            otherPlayer.getEntityId(),
                            GlowUtils.createDataValuesWithoutGlow(dataValues)
                    )
            );
        }

        ClientboundBundlePacket bundlePacket = new ClientboundBundlePacket(dataPackets);
        NMSConverter.serverPlayer(viewer).connection.send(bundlePacket);
    }

    private static class ModifyGlowDataPacketListener extends ChannelDuplexHandler {

        private final IntSet ids;

        ModifyGlowDataPacketListener(IntSet ids) {
            this.ids = ids;
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            if (!(msg instanceof ClientboundSetEntityDataPacket dataPacket) || !this.ids.contains(dataPacket.id())) {
                super.write(ctx, msg, promise);
                return;
            }

            ClientboundSetEntityDataPacket modifiedDataPacket = new ClientboundSetEntityDataPacket(
                    dataPacket.id(),
                    GlowUtils.createDataValuesWithGlow(dataPacket.packedItems())
            );
            super.write(ctx, modifiedDataPacket, promise);
        }
    }
}
