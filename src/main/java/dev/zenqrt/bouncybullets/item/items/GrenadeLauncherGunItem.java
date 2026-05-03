package dev.zenqrt.bouncybullets.item.items;

import dev.zenqrt.bouncybullets.BouncyBullets;
import dev.zenqrt.bouncybullets.game.games.BulletProperties;
import dev.zenqrt.bouncybullets.game.games.Gun;
import dev.zenqrt.bouncybullets.utils.ExplosionUtils;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public final class GrenadeLauncherGunItem extends GunItem {

    private static final int BOUNCES = 2;
    private static final int EXPLOSION_RADIUS = 6;

    public GrenadeLauncherGunItem(Gun gun) {
        super("grenade_launcher", Material.GOLDEN_HORSE_ARMOR, "Rocket Launcher", gun);
    }

    @Override
    protected void shootProjectile(Player player, BulletProperties bulletProperties) {
        Location eyeLocation = player.getEyeLocation();

        player.setVelocity(eyeLocation.getDirection().normalize().multiply(-1));

        TNTPrimed tnt = player.getWorld().spawn(eyeLocation, TNTPrimed.class);
        tnt.setSource(player);
        tnt.setFuseTicks(Integer.MAX_VALUE);
        tnt.setVelocity(eyeLocation.getDirection().normalize().multiply(bulletProperties.speed()));

        new TNTBounceTask(tnt, BOUNCES).runTaskTimer(BouncyBullets.getInstance(), 0, 1);
    }

    @Override
    protected Sound getShootingSound() {
        return Sound.sound(org.bukkit.Sound.ENTITY_GENERIC_EXPLODE, Sound.Source.PLAYER, 1, 2);
    }

    @Override
    protected Sound getReloadSound() {
        return Sound.sound(org.bukkit.Sound.BLOCK_BARREL_CLOSE, Sound.Source.PLAYER, 1, 0.75F);
    }

    private class TNTBounceTask extends BukkitRunnable {

        private static final Sound BOUNCE_SOUND = Sound.sound(org.bukkit.Sound.ENTITY_SLIME_JUMP_SMALL.key(), Sound.Source.MASTER, 0.5F, 1);

        private final TNTPrimed tnt;
        private final int maxBounces;
        private int currentBounces;
        private Vector previousVelocity;

        TNTBounceTask(TNTPrimed tnt, int maxBounces) {
            this.tnt = tnt;
            this.maxBounces = maxBounces;
            this.currentBounces = 0;
        }

        @Override
        public void run() {
            if (tnt.isDead()) {
                this.cancel();
                return;
            }

            if (currentBounces >= maxBounces) {
                ExplosionUtils.createExplosion(tnt.getLocation(), EXPLOSION_RADIUS, gun.getBulletProperties().maxDamage(), DamageSource.builder(DamageType.MOB_PROJECTILE).withCausingEntity(tnt.getSource()).build());
                tnt.remove();
                this.cancel();
                return;
            }

            if (tnt.isOnGround() && tnt.getVelocity().getY() <= 0) {
                currentBounces++;
                tnt.setVelocity(previousVelocity.multiply(new Vector(1, -1, 1)).multiply(0.5));
                tnt.getWorld().playSound(BOUNCE_SOUND, tnt);
            }

            previousVelocity = tnt.getVelocity();
        }
    }
}
