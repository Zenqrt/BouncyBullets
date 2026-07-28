package dev.zenqrt.bouncybullets.game.games;

import dev.zenqrt.bouncybullets.BouncyBulletsPlugin;
import dev.zenqrt.bouncybullets.event.events.GamePlayerDamageEvent;
import dev.zenqrt.bouncybullets.item.items.guns.GunItem;
import dev.zenqrt.bouncybullets.loadout.Loadout;
import dev.zenqrt.bouncybullets.hud.GameplayHud;
import dev.zenqrt.bouncybullets.hud.actionbar.BouncyBulletsHUD;
import dev.zenqrt.bouncybullets.utils.*;
import dev.zenqrt.bouncybullets.utils.entity.EntityUtils;
import dev.zenqrt.bouncybullets.utils.entity.LivingEntityFlags;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import net.kyori.adventure.sound.Sound;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class BouncyBulletGamePlayer {

    private static final NamespacedKey AIM_ZOOM_MODIFIER_KEY = BouncyBulletsPlugin.createKey("aim_zoom");
    private static final Sound AIM_SOUND = Sound.sound(Sounds.ITEM_ARMOR_EQUIP_GENERIC, Sound.Source.PLAYER, 0.5F, 0);

    private int deaths;
    private int kills;
    private int health;
    private int maxHealth;
    private boolean alive;
    private boolean reloading;
    private boolean aiming;
    private BouncyBulletGamePlayer lastDamager;

    private Vector deltaMovement;
    private float deltaYaw;
    private float deltaPitch;

    private boolean invisible;
    private ItemStack[] pastArmorContents;

    private final BouncyBulletsHUD oldHud;
    private final GameplayHud hud;
    private Loadout loadout;
    private final Player player;
    private final UUID uuid;

    public BouncyBulletGamePlayer(Player player, Loadout loadout) {
        this.uuid = player.getUniqueId();
        this.player = player;
        this.loadout = loadout;
        this.oldHud = new BouncyBulletsHUD();
        this.hud = new GameplayHud();

        this.kills = 0;
        this.deaths = 0;
        this.alive = false;
        this.reloading = false;
        this.aiming = false;

        this.invisible = false;
    }

    public void hurt(int damage, @Nullable BouncyBulletGamePlayer damager) {
        GamePlayerDamageEvent event = new GamePlayerDamageEvent(this, damage, damager);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return;

        int newHealth = this.health - event.getDamage();

        if (newHealth <= 0) {
            this.player.kill();
            return;
        }

        setHealth(newHealth);
        this.hud.setHealth(newHealth);

        this.lastDamager = event.getDamager();

        SoundUtils.playSoundFromPlayer(this.player, Sound.sound(Sounds.ENTITY_PLAYER_HURT, Sound.Source.PLAYER, 0.5F, 2));
    }

    public BouncyBulletGamePlayer getLastDamager() {
        return lastDamager;
    }

    public void setHealth(int newHealth) {
        int lowHealthIndicator = (int) (this.maxHealth / 3F);

        if (this.health > lowHealthIndicator && newHealth <= lowHealthIndicator)
            PlayerVisualEffects.showLowHealthEffect(this.player);
        else
            PlayerVisualEffects.hideLowHealthEffect(this.player);

        this.health = newHealth;
        this.hud.setHealth(newHealth);
    }

    public int getHealth() {
        return health;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;

        this.hud.setMaxHealth(maxHealth);
    }

    public int getMaxHealth() {
        return maxHealth;
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

    @SuppressWarnings("UnstableApiUsage")
    public void startAiming(BouncyBulletGame game, GunItem gunItem, ItemStack itemStack) {
        this.aiming = true;

        AttributeInstance movementSpeed = PlayerUtils.requireNonNullAttribute(this.player, Attribute.MOVEMENT_SPEED);
        AttributeModifier zoomModifier = new AttributeModifier(
                AIM_ZOOM_MODIFIER_KEY,
                -0.15 * gunItem.getGunProperties().scopeMagnifyMultiplier(),
                AttributeModifier.Operation.ADD_SCALAR
        );

        movementSpeed.addTransientModifier(zoomModifier);

        itemStack.setData(
                DataComponentTypes.CUSTOM_MODEL_DATA,
                CustomModelData.customModelData()
                        .addString("aim")
                        .build()
        );

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

    @SuppressWarnings("UnstableApiUsage")
    public void stopAiming(BouncyBulletGame game, ItemStack itemStack) {
        this.aiming = false;

        AttributeInstance movementSpeed = PlayerUtils.requireNonNullAttribute(this.player, Attribute.MOVEMENT_SPEED);

        itemStack.setData(
                DataComponentTypes.CUSTOM_MODEL_DATA,
                CustomModelData.customModelData().build()
        );

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

    public void setDeltaMovement(Vector deltaMovement) {
        this.deltaMovement = deltaMovement;
    }

    public Vector getDeltaMovement() {
        return deltaMovement;
    }

    public void setDeltaYaw(float deltaYaw) {
        this.deltaYaw = deltaYaw;
    }

    public float getDeltaYaw() {
        return deltaYaw;
    }

    public void setDeltaPitch(float deltaPitch) {
        this.deltaPitch = deltaPitch;
    }

    public float getDeltaPitch() {
        return deltaPitch;
    }

    public BouncyBulletsHUD getOldHud() {
        return oldHud;
    }

    public GameplayHud getHud() {
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
