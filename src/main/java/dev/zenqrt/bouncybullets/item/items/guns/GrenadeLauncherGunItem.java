package dev.zenqrt.bouncybullets.item.items.guns;

import com.destroystokyo.paper.ParticleBuilder;
import dev.zenqrt.bouncybullets.BouncyBulletsPlugin;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import dev.zenqrt.bouncybullets.loadout.gun.BulletProperties;
import dev.zenqrt.bouncybullets.loadout.gun.GunProperties;
import dev.zenqrt.bouncybullets.utils.ExplosionUtils;
import dev.zenqrt.bouncybullets.utils.Sounds;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public final class GrenadeLauncherGunItem extends GunItem {

    private static final int EXPLOSION_RADIUS = 6;

    public GrenadeLauncherGunItem(GunProperties gunProperties, BulletProperties bulletProperties) {
        super("grenade_launcher", Material.GOLDEN_HORSE_ARMOR, "Grenade Launcher", gunProperties, bulletProperties);
    }

    @Override
    protected void shootProjectile(BouncyBulletGame game, Player player, BulletProperties bulletProperties) {
        Location eyeLocation = player.getEyeLocation();

        player.setVelocity(eyeLocation.getDirection().normalize().multiply(-1));

        TNTPrimed tnt = player.getWorld().spawn(eyeLocation, TNTPrimed.class);
        tnt.setSource(player);
        tnt.setFuseTicks(Integer.MAX_VALUE);
        tnt.setVelocity(eyeLocation.getDirection().normalize().multiply(bulletProperties.speed()));

        BouncyBulletGamePlayer gamePlayer = game.findPlayerOrThrow(player.getUniqueId());

        new TNTBounceTask(gamePlayer, player, tnt, bulletProperties.numberOfBounces()).runTaskTimer(BouncyBulletsPlugin.getInstance(), 0, 1);
    }

    @Override
    protected void useGun(BouncyBulletGame game, BouncyBulletGamePlayer gamePlayer, Player player, ItemStack itemStack) {
        long currentGameTime = player.getServer().getCurrentTick();
        int ammo = getAmmo(itemStack);

        if (ammo <= 0)
            return;

        if (super.lastShootTicks.containsKey(player.getUniqueId())) {
            long lastShootTick = super.lastShootTicks.get(player.getUniqueId());
            long tickInterval = currentGameTime - lastShootTick;

            if (tickInterval < super.gunProperties.shootDelayTicks()) {
                return;
            }
        }

        shootGun(game, player, gamePlayer.getHud(), itemStack);

        super.lastShootTicks.put(player.getUniqueId(), currentGameTime);
    }

    @Override
    protected Sound getShootingSound() {
        return Sound.sound(Sounds.ENTITY_GENERIC_EXPLODE, Sound.Source.PLAYER, 1, 2);
    }

    @Override
    protected Sound getReloadSound() {
        return Sound.sound(Sounds.BLOCK_BARREL_CLOSE, Sound.Source.PLAYER, 1, 0.75F);
    }

    private class TNTBounceTask extends BukkitRunnable {

        private static final Sound BOUNCE_SOUND = Sound.sound(Sounds.ENTITY_SLIME_JUMP, Sound.Source.MASTER, 0.5F, 1);

        private final BouncyBulletGamePlayer gamePlayer;
        private final Player source;
        private final TNTPrimed tnt;
        private final int maxBounces;
        private int currentBounces;
        private Vector previousVelocity;

        TNTBounceTask(BouncyBulletGamePlayer gamePlayer, Player source, TNTPrimed tnt, int maxBounces) {
            this.gamePlayer = gamePlayer;
            this.source = source;
            this.tnt = tnt;
            this.maxBounces = maxBounces;
            this.currentBounces = 0;
        }

        @Override
        public void run() {
            if (this.tnt.isDead() || this.gamePlayer.isDead()) {
                this.cancel();
                return;
            }

            ParticleBuilder particleBuilder =
                    this.currentBounces == 0 ? createTntTrailFirstBounce()
                            : this.currentBounces == 1 ? createTntTrailSecondBounce()
                            : createTntTrailThirdBounce();

            particleBuilder
                    .location(this.tnt.getLocation())
                    .spawn();

            if (this.currentBounces >= this.maxBounces || this.source.isSneaking()) {
                double damageMultiplier = 1 + this.currentBounces * 0.2;
                double normalDamage = GrenadeLauncherGunItem.super.bulletProperties.maxDamage();

                ExplosionUtils.createExplosion(
                        this.tnt.getLocation(),
                        EXPLOSION_RADIUS,
                        normalDamage * damageMultiplier,
                        DamageSource.builder(DamageType.MOB_PROJECTILE)
                                .withDirectEntity(this.tnt)
                                .withCausingEntity(this.source).build()
                );

                this.tnt.remove();
                this.cancel();
                return;
            }

            if (this.tnt.isOnGround() && this.tnt.getVelocity().getY() <= 0) {
                this.currentBounces++;
                this.tnt.setVelocity(this.previousVelocity.multiply(new Vector(0.5, -0.85, 0.5)));
                this.tnt.getWorld().playSound(BOUNCE_SOUND, tnt);
            }

            this.previousVelocity = this.tnt.getVelocity();
        }
    }

    private static ParticleBuilder createTntTrailFirstBounce() {
        return Particle.SMOKE.builder()
                .extra(0)
                .count(1)
                .force(true);
    }

    private static ParticleBuilder createTntTrailSecondBounce() {
        return Particle.FLAME.builder()
                .extra(0)
                .count(1)
                .force(true);
    }

    private static ParticleBuilder createTntTrailThirdBounce() {
        return Particle.SCULK_SOUL.builder()
                .extra(0)
                .offset(0.25, 0.25, 0.25)
                .count(5)
                .force(true);
    }
}
