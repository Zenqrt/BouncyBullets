package dev.zenqrt.bouncybullets.item.items.guns;

import com.destroystokyo.paper.ParticleBuilder;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import dev.zenqrt.bouncybullets.loadout.gun.BulletProperties;
import dev.zenqrt.bouncybullets.loadout.gun.GunProperties;
import dev.zenqrt.bouncybullets.player.BouncyBulletsHUD;
import dev.zenqrt.bouncybullets.tasks.ShootBulletTask;
import dev.zenqrt.bouncybullets.utils.PlayerUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public abstract class BulletGunItem extends GunItem {

    private static final int INTERACT_EVENT_TICK_DELAY = 4;

    private final TipOffset tipOffset;
    private final TipOffset tipOffsetAiming;

    public BulletGunItem(
            String key,
            String displayName,
            GunProperties gunProperties,
            BulletProperties bulletProperties,
            TipOffset tipOffset,
            TipOffset tipOffsetAiming
    ) {
        super(key, displayName, gunProperties, bulletProperties);

        this.tipOffset = tipOffset;
        this.tipOffsetAiming = tipOffsetAiming;
    }

    protected abstract ParticleBuilder getBulletParticleBuilder();

    @Override
    protected BukkitTask startReloading(Plugin plugin, BouncyBulletGamePlayer gamePlayer, Player player, ItemStack itemStack, int ammo) {
        gamePlayer.getHud().addDisplay("reloading", Component.text("Reloading...", NamedTextColor.WHITE));
        gamePlayer.getHud().updateHudText();

        AttributeInstance movementSpeed = PlayerUtils.requireNonNullAttribute(player, Attribute.MOVEMENT_SPEED);

        gamePlayer.setReloading(true);

        movementSpeed.removeModifier(RELOAD_SLOWDOWN_MODIFIER);
        movementSpeed.addTransientModifier(RELOAD_SLOWDOWN_MODIFIER);

        player.setCooldown(itemStack.getType(), super.gunProperties.reloadTicks()); // TODO Change cooldown mechanics
        player.playSound(getReloadSound());

        return Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> {
                    itemStack.editMeta(meta -> {
                        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();

                        setAmmo(dataContainer, BulletGunItem.super.gunProperties.magazineSize());
                    });

                    movementSpeed.removeModifier(RELOAD_SLOWDOWN_MODIFIER);
                    player.setCooldown(itemStack.getType(), 0);

                    gamePlayer.getHud().updateAmmo(
                            BulletGunItem.super.gunProperties.magazineSize(),
                            BulletGunItem.super.gunProperties.magazineSize()
                    );
                    gamePlayer.getHud().removeDisplay("reloading");
                    gamePlayer.getHud().updateHudText();
                    gamePlayer.setReloading(false);
                },
                BulletGunItem.super.gunProperties.reloadTicks()
        );
    }

    @Override
    protected void shootProjectile(BouncyBulletGame game, BouncyBulletGamePlayer gamePlayer, BulletProperties bulletProperties) {
        Player player = gamePlayer.getPlayer();
        Location eyeLocation = predictEyeLocation(gamePlayer);

        Vector forward = eyeLocation.getDirection().normalize();
        Vector up = new Vector(0, 1, 0);
        Vector right = forward.clone()
                .crossProduct(up)
                .normalize();
        Location gunTipPos;

        if (gamePlayer.isAiming()) {
            gunTipPos = eyeLocation.clone()
                    .add(forward.multiply(this.tipOffsetAiming.forward))
                    .add(right.multiply(this.tipOffsetAiming.right));
        } else {
            gunTipPos = eyeLocation.clone()
                    .add(forward.multiply(this.tipOffset.forward))
                    .add(right.multiply(this.tipOffset.right))
                    .subtract(0, this.tipOffset.down, 0);
        }

//        Location bulletDestroyPos = player.getEyeLocation().clone()
//                .subtract(0, 100, 0);
//        Color fireColor = Color.fromRGB(252, 158, 38);

        Particle.END_ROD.builder()
//                .data(new Particle.Trail(bulletDestroyPos, fireColor, 1))
                .location(gunTipPos)
//                .offset(0.025, 0.025, 0.025)
                .extra(1000)
                .count(1)
                .spawn();

        new ShootBulletTask(
                player,
                gunTipPos,
                player.getEyeLocation().getDirection().normalize(),
                bulletProperties,
                gamePlayer.isAiming() ? this.gunProperties.spreadRangeFocused() : this.gunProperties.spreadRange()
        ).runTaskTimer(game.getPlugin(), 0, 1);
    }

    private static Location predictEyeLocation(BouncyBulletGamePlayer gamePlayer) {
        Location eyeLocation = gamePlayer.getPlayer().getEyeLocation();
        Vector deltaMovement = gamePlayer.getDeltaMovement();

        if (deltaMovement.isZero())
            return eyeLocation.clone()
                    .addRotation(gamePlayer.getDeltaYaw(), gamePlayer.getDeltaPitch());

        Vector drift = deltaMovement.clone()
                .normalize()
                .multiply(0.1);

        return eyeLocation.clone()
                .addRotation(gamePlayer.getDeltaYaw(), gamePlayer.getDeltaPitch())
                .add(deltaMovement)
                .add(drift);
    }

    @Override
    protected void useGun(BouncyBulletGame game, BouncyBulletGamePlayer gamePlayer, Player player, ItemStack itemStack) {
        if (super.gunProperties.shootDelayTicks() < INTERACT_EVENT_TICK_DELAY) {
            long shootDivisions = INTERACT_EVENT_TICK_DELAY / super.gunProperties.shootDelayTicks();

            tryShootGun(game, gamePlayer, gamePlayer.getHud(), itemStack);

            for (int i = 1; i < shootDivisions; i++) {
                Bukkit.getScheduler().runTaskLater(
                        game.getPlugin(),
                        () -> tryShootGun(game, gamePlayer, gamePlayer.getHud(), itemStack),
                        (long) i * super.gunProperties.shootDelayTicks()
                );
            }

            return;
        }

        tryShootGun(game, gamePlayer, gamePlayer.getHud(), itemStack);
    }

    private void tryShootGun(BouncyBulletGame game, BouncyBulletGamePlayer gamePlayer, BouncyBulletsHUD hud, ItemStack itemStack) {
        Player player = gamePlayer.getPlayer();

        long currentGameTime = player.getServer().getCurrentTick();

        if (getAmmo(itemStack) <= 0)
            return;

        if (super.lastShootTicks.containsKey(player.getUniqueId())) {
            long lastShootTick = super.lastShootTicks.get(player.getUniqueId());
            long tickInterval = currentGameTime - lastShootTick;

            if (tickInterval < super.gunProperties.shootDelayTicks()) {
                return;
            }
        }


        shootGun(game, gamePlayer, hud, itemStack);
        super.lastShootTicks.put(player.getUniqueId(), currentGameTime);
    }

    public record TipOffset(float down, float forward, float right) {}
}
