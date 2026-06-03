package dev.zenqrt.bouncybullets.game.games;

import dev.zenqrt.bouncybullets.BouncyBulletsPlugin;
import dev.zenqrt.bouncybullets.item.items.guns.GunItem;
import dev.zenqrt.bouncybullets.loadout.Loadout;
import dev.zenqrt.bouncybullets.player.BouncyBulletsHUD;
import dev.zenqrt.bouncybullets.utils.NMSConverter;
import dev.zenqrt.bouncybullets.utils.PlayerUtils;
import dev.zenqrt.bouncybullets.utils.Sounds;
import dev.zenqrt.bouncybullets.utils.entity.EntityUtils;
import dev.zenqrt.bouncybullets.utils.entity.LivingEntityFlags;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class BouncyBulletGamePlayer {

    private static final NamespacedKey AIM_ZOOM_MODIFIER_KEY = BouncyBulletsPlugin.createKey("aim_zoom");
    private static final Sound AIM_SOUND = Sound.sound(Sounds.UI_BUTTON_CLICK, Sound.Source.MASTER, 0.5F, 2F);
    private static final Title AIM_CROSSHAIR_TITLE = Title.title(
            Component.empty(),
            Component.text("^", NamedTextColor.DARK_GRAY).decorate(TextDecoration.BOLD),
            Title.Times.times(Duration.ZERO, Duration.ofDays(1), Duration.ZERO));

    private int deaths;
    private int kills;
    private boolean alive;
    private boolean reloading;
    private boolean aiming;

    private boolean invisible;
    private ItemStack[] pastArmorContents;

    private final BouncyBulletsHUD hud;
    private Loadout loadout;
    private final Player player;
    private final UUID uuid;

    public BouncyBulletGamePlayer(Player player, Loadout loadout) {
        this.uuid = player.getUniqueId();
        this.player = player;
        this.loadout = loadout;
        this.hud = new BouncyBulletsHUD();

        this.kills = 0;
        this.deaths = 0;
        this.alive = false;
        this.reloading = false;
        this.aiming = false;

        this.invisible = false;
    }

    public void hide() {
        this.invisible = true;
        this.pastArmorContents = this.player.getInventory().getArmorContents();

        this.player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, true, false, true));
        this.player.getInventory().setArmorContents(new ItemStack[]{null, null, null, null});
    }

    public void reveal() {
        this.invisible = false;
        this.player.removePotionEffect(PotionEffectType.INVISIBILITY);
        this.player.getInventory().setArmorContents(this.pastArmorContents);
    }

    public boolean isInvisible() {
        return invisible;
    }

    public void setReloading(boolean reloading) {
        this.reloading = reloading;
    }

    public boolean isReloading() {
        return reloading;
    }

    public void startAiming(BouncyBulletGame game, GunItem gunItem) {
        this.aiming = true;

        AttributeInstance movementSpeed = PlayerUtils.requireNonNullAttribute(this.player, Attribute.MOVEMENT_SPEED);
        AttributeModifier zoomModifier = new AttributeModifier(
                AIM_ZOOM_MODIFIER_KEY,
                -0.15 * gunItem.getGunProperties().scopeMagnifyMultiplier(),
                AttributeModifier.Operation.ADD_SCALAR
        );

        movementSpeed.addTransientModifier(zoomModifier);
        this.player.showTitle(AIM_CROSSHAIR_TITLE);
        this.player.playSound(AIM_SOUND, Sound.Emitter.self());

        ServerPlayer nmsPlayer = NMSConverter.serverPlayer(this.player);

        for (BouncyBulletGamePlayer otherGamePlayer : game.getPlayers().values()) {
            if (otherGamePlayer.equals(this))
                continue;

            List<SynchedEntityData.DataValue<?>> dataValues = Objects.requireNonNullElse(
                    nmsPlayer.getEntityData().getNonDefaultValues(),
                    new ArrayList<>()
            );

            ServerPlayer nmsOtherPlayer = NMSConverter.serverPlayer(otherGamePlayer.getPlayer());
            nmsOtherPlayer.connection.send(
                    new ClientboundSetEntityDataPacket(
                            this.player.getEntityId(),
                            EntityUtils.createValuesWithLivingEntityFlag(
                                    LivingEntityFlags.LIVING_ENTITY_FLAG_IS_USING,
                                    dataValues
                            )
                    )
            );
        }
    }

    public void stopAiming(BouncyBulletGame game) {
        this.aiming = false;

        AttributeInstance movementSpeed = PlayerUtils.requireNonNullAttribute(this.player, Attribute.MOVEMENT_SPEED);

        this.player.clearTitle();
        movementSpeed.removeModifier(AIM_ZOOM_MODIFIER_KEY);

        ServerPlayer nmsPlayer = NMSConverter.serverPlayer(this.player);

        for (BouncyBulletGamePlayer otherGamePlayer : game.getPlayers().values()) {
            if (otherGamePlayer.equals(this))
                continue;

            List<SynchedEntityData.DataValue<?>> dataValues = Objects.requireNonNullElse(
                    nmsPlayer.getEntityData().getNonDefaultValues(),
                    new ArrayList<>()
            );

            ServerPlayer nmsOtherPlayer = NMSConverter.serverPlayer(otherGamePlayer.getPlayer());
            nmsOtherPlayer.connection.send(
                    new ClientboundSetEntityDataPacket(
                            this.player.getEntityId(),
                            EntityUtils.createValuesWithoutLivingEntityFlag(
                                    LivingEntityFlags.LIVING_ENTITY_FLAG_IS_USING,
                                    dataValues
                            )
                    )
            );
        }
    }

    public boolean isAiming() {
        return aiming;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public boolean isDead() {
        return !alive;
    }

    public boolean isAlive() {
        return alive;
    }

    public void addDeath() {
        this.deaths++;
    }

    public int getDeaths() {
        return deaths;
    }

    public void addKill() {
        this.kills++;
    }

    public int getKills() {
        return kills;
    }

    public BouncyBulletsHUD getHud() {
        return hud;
    }

    public void setLoadout(Loadout loadout) {
        this.loadout = loadout;
    }

    public Loadout getLoadout() {
        return loadout;
    }

    public Player getPlayer() {
        return player;
    }

    public UUID getUuid() {
        return uuid;
    }
}
