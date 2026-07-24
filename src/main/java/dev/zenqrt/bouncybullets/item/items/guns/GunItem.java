package dev.zenqrt.bouncybullets.item.items.guns;

import com.google.common.base.Preconditions;
import dev.zenqrt.bouncybullets.BouncyBulletsPlugin;
import dev.zenqrt.bouncybullets.event.events.GunShootEvent;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import dev.zenqrt.bouncybullets.item.GameItem;
import dev.zenqrt.bouncybullets.loadout.gun.BulletProperties;
import dev.zenqrt.bouncybullets.loadout.gun.GunProperties;
import dev.zenqrt.bouncybullets.packet.PacketSender;
import dev.zenqrt.bouncybullets.player.hud.GameplayHud;
import dev.zenqrt.bouncybullets.player.hud.actionbar.BouncyBulletsHUD;
import dev.zenqrt.bouncybullets.utils.MiniMessageUtils;
import dev.zenqrt.bouncybullets.utils.NMSConverter;
import dev.zenqrt.bouncybullets.utils.PlayerUtils;
import dev.zenqrt.bouncybullets.utils.Sounds;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers;
import io.papermc.paper.datacomponent.item.SwingAnimation;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public abstract class GunItem extends GameItem {

    protected static final AttributeModifier RELOAD_SLOWDOWN_MODIFIER = new AttributeModifier(
            BouncyBulletsPlugin.createKey("reload_slowdown"),
            -0.05,
            AttributeModifier.Operation.ADD_NUMBER
    );
    private static final NamespacedKey AMMO_KEY = new NamespacedKey(BouncyBulletsPlugin.getInstance(), "ammo");

    private final Map<UUID, BukkitTask> reloadTaskMap = new HashMap<>();
    private final Map<UUID, ScheduledTask> recoilAnimationMap = new HashMap<>();
    protected final Map<UUID, Long> lastShootTicks = new HashMap<>();

    protected final GunProperties gunProperties;
    protected final BulletProperties bulletProperties;

    @SuppressWarnings("UnstableApiUsage")
    public GunItem(String key, Component displayName, GunProperties gunProperties, BulletProperties bulletProperties, DataComponentsBuilder dataComponentsBuilder) {
        super(
                key,
                Material.BOW,
                displayName,
                buildGunPropertyDescription(bulletProperties),
                dataComponentsBuilder
                        .addData(
                                DataComponentTypes.ATTRIBUTE_MODIFIERS,
                                ItemAttributeModifiers.itemAttributes()
                                        .addModifier(
                                                Attribute.ATTACK_SPEED,
                                                new AttributeModifier(
                                                        BouncyBulletsPlugin.createKey("gun_pullout_speed"),
                                                        -pullOutTicksToAttackSpeed(gunProperties.pullOutTicks()),
                                                        AttributeModifier.Operation.ADD_NUMBER,
                                                        EquipmentSlotGroup.HAND
                                                )
                                        )
                                        .build()
                        )
                        .addData(
                                DataComponentTypes.TOOLTIP_DISPLAY,
                                TooltipDisplay.tooltipDisplay()
                                        .addHiddenComponents(DataComponentTypes.ATTRIBUTE_MODIFIERS)
                                        .build()
                        )
                        .addData(
                                DataComponentTypes.ITEM_MODEL,
                                new NamespacedKey("bouncybullets", key)
                        )
        );

        this.gunProperties = gunProperties;
        this.bulletProperties = bulletProperties;
    }

    private static double pullOutTicksToAttackSpeed(int pullOutTicks) {
        return 4 - 20D / pullOutTicks;
    }

    public GunItem(String key, String displayName, GunProperties gunProperties, BulletProperties bulletProperties, DataComponentsBuilder dataComponentsBuilder) {
        this(
                key,
                Component.text(displayName, NamedTextColor.YELLOW)
                        .append(
                                Component.text(" (", NamedTextColor.GRAY)
                                        .append(Component.keybind("key.swapOffhand"))
                                        .append(Component.text(" to reload)"))
                        ),
                gunProperties,
                bulletProperties,
                dataComponentsBuilder
        );
    }

    public GunItem(String key, String displayName, GunProperties gunProperties, BulletProperties bulletProperties) {
        this(key, displayName, gunProperties, bulletProperties, dataComponentsBuilder());
    }

    protected abstract BukkitTask startReloading(Plugin plugin, BouncyBulletGamePlayer gamePlayer, Player player, ItemStack itemStack, int currentAmmo);

    protected abstract void useGun(BouncyBulletGame game, BouncyBulletGamePlayer gamePlayer, Player player, ItemStack itemStack);
    protected abstract void shootProjectile(BouncyBulletGame game, BouncyBulletGamePlayer gamePlayer, BulletProperties bulletProperties);
    protected abstract Sound getShootingSound();

    @Override
    public void onHeld(BouncyBulletGame game, Player player, ItemStack itemStack, ItemStack previousItemStack) {
        final BouncyBulletGamePlayer gamePlayer = game.findPlayerOrThrow(player.getUniqueId());

        int ammo = getAmmo(itemStack);

        gamePlayer.getHud().setAmmo(ammo);
        gamePlayer.getHud().showAmmo();

        player.setCooldown(itemStack.getType(), this.getGunProperties().pullOutTicks());
    }

    @Override
    public void onUnheld(BouncyBulletGame game, Player player, ItemStack itemStack, ItemStack newItemStack) {
        final BouncyBulletGamePlayer gamePlayer = game.findPlayerOrThrow(player.getUniqueId());

        gamePlayer.getHud().hideAmmo();

        if (gamePlayer.isAiming())
            gamePlayer.stopAiming(game, itemStack);

        if (gamePlayer.isReloading()) {
            gamePlayer.setReloading(false);

            AttributeInstance movementSpeed = PlayerUtils.requireNonNullAttribute(player, Attribute.MOVEMENT_SPEED);
            movementSpeed.removeModifier(RELOAD_SLOWDOWN_MODIFIER);

            gamePlayer.getOldHud().removeDisplay("reloading");
            gamePlayer.getOldHud().updateHudText();
            player.stopSound(getReloadSound());

            BukkitTask reloadTask = this.reloadTaskMap.remove(gamePlayer.getUuid());

            if (reloadTask != null)
                reloadTask.cancel();
        }

    }

    @Override
    public void onInteract(BouncyBulletGame game, Player player, ItemStack itemStack, PlayerInteractEvent event) {
        event.setCancelled(true);

        BouncyBulletGamePlayer gamePlayer = game.findPlayerOrThrow(player.getUniqueId());

        if (event.getAction().isLeftClick()) {
            if (gamePlayer.isAiming())
                gamePlayer.stopAiming(game, itemStack);
            else if (!gamePlayer.isReloading())
                gamePlayer.startAiming(game, this, itemStack);

        } else if (event.getAction().isRightClick()) {
            if (player.hasCooldown(this.material))
                return;

            useGun(game, gamePlayer, player, itemStack);
        }
    }

    protected final void shootGun(BouncyBulletGame game, BouncyBulletGamePlayer gamePlayer, BouncyBulletsHUD hud, ItemStack itemStack) {
        Player player = gamePlayer.getPlayer();

        GunShootEvent event = new GunShootEvent(this, player, this.bulletProperties);
        Bukkit.getPluginManager().callEvent(event);

        player.getWorld().playSound(getShootingSound(), player);

        ServerPlayer nmsPlayer = NMSConverter.serverPlayer(player);

        playCameraEffect(game.getPlugin(), nmsPlayer, gamePlayer.isAiming());

        shootProjectile(game, gamePlayer, event.getBulletProperties());
        useAmmo(itemStack, gamePlayer.getHud());
    }

    private void playCameraEffect(Plugin plugin, ServerPlayer nmsPlayer, boolean focused) {
        Abilities abilities = new Abilities();
        abilities.apply(nmsPlayer.getAbilities().pack());
        abilities.setWalkingSpeed(0.08F);

        ClientboundPlayerAbilitiesPacket abilitiesPacket = new ClientboundPlayerAbilitiesPacket(abilities);
        nmsPlayer.connection.send(abilitiesPacket);

        ClientboundPlayerAbilitiesPacket resetAbilitiesPacket = new ClientboundPlayerAbilitiesPacket(nmsPlayer.getAbilities());

        Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> nmsPlayer.connection.send(resetAbilitiesPacket),
                3
        );

        if (focused)
            showRecoilAnimation(
                    nmsPlayer,
                    this.gunProperties.recoilPitchFocused(),
                    this.gunProperties.recoilYawFocused(),
                    this.gunProperties.spreadRangeFocused(),
                    plugin,
                    ThreadLocalRandom.current()
            );
        else
            showRecoilAnimation(
                    nmsPlayer,
                    this.gunProperties.recoilPitch(),
                    this.gunProperties.recoilYaw(),
                    this.gunProperties.spreadRange(),
                    plugin,
                    ThreadLocalRandom.current()
            );

    }

    private void showRecoilAnimation(ServerPlayer nmsPlayer, double pitchMag, double yawMag, double spread, Plugin plugin, Random random) {
        float cycles = 2;
        long startTime = System.currentTimeMillis();

        ScheduledTask existingAnimationTask = this.recoilAnimationMap.remove(nmsPlayer.getUUID());

        if (existingAnimationTask != null)
            existingAnimationTask.cancel();

        double pitchSpread = random.nextDouble(spread);
        double yawSpread = random.nextDouble(spread);

        float pitchAmplitude = (float) (pitchMag + pitchSpread);
        float yawAmplitude = (float) (yawMag + yawSpread);

        AtomicReference<Float> xRotAngle = new AtomicReference<>(pitchAmplitude);
        AtomicReference<Float> yRotAngle = new AtomicReference<>(yawAmplitude);

        ClientboundPlayerPositionPacket startPacket = createHeadRotationPacket(-pitchAmplitude, yawAmplitude);

        PacketSender.sendNow(nmsPlayer, startPacket);

        /*
        - Calculate angle with decaying exponential sine
        - Get difference from that and xRotAngle
        - Save current angle to xRotAngle
        - Apply difference to player
         */
        ScheduledTask animationTask = Bukkit.getAsyncScheduler().runAtFixedRate(
                plugin,
                task -> {
                    float elapsed = (System.currentTimeMillis() - startTime) / 1000f;

                    if (elapsed >= 4 * Math.PI * cycles - 1) {
                        task.cancel();
                        return;
                    }

                    float shakeXRot;
                    float cutoff = 0.15F;

                    if (elapsed > cutoff) {
                        shakeXRot = (float) -(Math.exp(-2 * elapsed)
                                * Math.sin(Math.PI * (elapsed - cutoff))
                                * 2.25
                        );
                    } else {
                        shakeXRot = (float) (pitchAmplitude * Math.sin(Math.PI * elapsed / cutoff));
                    }
                    float shakeYRot = (float) (Math.exp(-5 * elapsed)
                            * Math.cos(elapsed)
                            * yawAmplitude
                    );

                    float deltaXRot = shakeXRot - xRotAngle.getAndSet(shakeXRot);
                    float deltaYRot = shakeYRot - yRotAngle.getAndSet(shakeYRot);

                    ClientboundPlayerPositionPacket positionPacket = createHeadRotationPacket(-deltaXRot, deltaYRot);

                    PacketSender.sendNow(nmsPlayer, positionPacket);
                },
                0,
                10,
                TimeUnit.MILLISECONDS
        );

        this.recoilAnimationMap.put(nmsPlayer.getUUID(), animationTask);
    }

    public static ClientboundPlayerPositionPacket createHeadRotationPacket(float xRot, float yRot) {
        return new ClientboundPlayerPositionPacket(
                ThreadLocalRandom.current().nextInt(10000, 1000000),
                new PositionMoveRotation(
                        Vec3.ZERO,
                        Vec3.ZERO,
                        yRot,
                        xRot
                ),
                Set.of(
                        Relative.X,
                        Relative.Y,
                        Relative.Z,
                        Relative.DELTA_X,
                        Relative.DELTA_Y,
                        Relative.DELTA_Z,
                        Relative.X_ROT,
                        Relative.Y_ROT
                )
        );
    }

    protected final void useAmmo(ItemStack itemStack, GameplayHud hud) {
        itemStack.editMeta(meta -> {
            PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
            int ammo = dataContainer.getOrDefault(AMMO_KEY, PersistentDataType.INTEGER, this.gunProperties.magazineSize());
            int newAmmo = ammo - 1;

            dataContainer.set(AMMO_KEY, PersistentDataType.INTEGER, newAmmo);

            hud.setAmmo(newAmmo);
        });
    }

    public final void handleReload(Plugin plugin, BouncyBulletGamePlayer gamePlayer, Player player, ItemStack itemStack) {
        Preconditions.checkArgument(!gamePlayer.isReloading(), "gamePlayer must not be reloading");

        int ammo = getAmmo(itemStack);

        if (ammo >= this.gunProperties.magazineSize())
            return;

        this.reloadTaskMap.put(gamePlayer.getUuid(), startReloading(plugin, gamePlayer, player, itemStack, ammo));
    }

    protected Sound getReloadSound() {
        return Sound.sound(Sounds.ITEM_ARMOR_EQUIP_CHAIN, Sound.Source.PLAYER, 1, 1);
    }

    protected void setAmmo(PersistentDataContainer dataContainer, int ammo) {
        dataContainer.set(AMMO_KEY, PersistentDataType.INTEGER, ammo);
    }

    public int getAmmo(ItemStack itemStack) {
        return itemStack.getItemMeta().getPersistentDataContainer().getOrDefault(AMMO_KEY, PersistentDataType.INTEGER, this.gunProperties.magazineSize());
    }

    private static List<Component> buildGunPropertyDescription(BulletProperties bulletProperties) {
        return MiniMessageUtils.wordWrapLore(List.of(
                "<gray>Damage: <red>" + bulletProperties.maxDamage() + "❤",
                "<gray>Speed: <yellow>" + bulletProperties.speed() + " blocks/s",
                "<gray>Bounces: <yellow>" + bulletProperties.numberOfBounces()

        ), 30);
    }

    public GunProperties getGunProperties() {
        return gunProperties;
    }

    public BulletProperties getBulletProperties() {
        return bulletProperties;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public ItemStack buildItemStack() {
        ItemStack itemStack = super.buildItemStack();
        itemStack.setData(
                DataComponentTypes.SWING_ANIMATION,
                SwingAnimation.swingAnimation()
                        .type(SwingAnimation.Animation.NONE)
                        .duration(1)
        );

        return itemStack;
    }
}
