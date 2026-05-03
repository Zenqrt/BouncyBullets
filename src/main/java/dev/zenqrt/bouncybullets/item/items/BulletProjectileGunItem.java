package dev.zenqrt.bouncybullets.item.items;

import com.destroystokyo.paper.ParticleBuilder;
import dev.zenqrt.bouncybullets.BouncyBullets;
import dev.zenqrt.bouncybullets.game.games.BulletProperties;
import dev.zenqrt.bouncybullets.game.games.Gun;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public abstract class BulletProjectileGunItem extends GunItem {

    private static final Sound HIT_SOUND = Sound.sound(org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP.key(), Sound.Source.PLAYER, 1, 2);

    public BulletProjectileGunItem(String key, Material material, Component displayName, Gun gun) {
        super(key, material, displayName, gun);
    }

    public BulletProjectileGunItem(String key, Material material, String displayName, Gun gun) {
        super(key, material, displayName, gun);
    }

    protected abstract ParticleBuilder getBulletParticleBuilder();

    @Override
    protected void shootProjectile(Player player, BulletProperties bulletProperties) {
        new ShootBulletTask(player, player.getEyeLocation(), bulletProperties, player.hasPotionEffect(PotionEffectType.SLOW)).runTaskTimer(BouncyBullets.getInstance(), 0, 1);
    }

    private class ShootBulletTask extends BukkitRunnable {

        private final Player shooter;
        private final BulletProperties bulletProperties;
        private final Location shotFrom;
        private Location bounceLocation;
        private Location lastBulletLocation;
        private Vector currentDirection;
        private int bounces = 0;
        private int tickSinceBounce = 0;
        private int currentTick = 0;

        private ShootBulletTask(Player shooter, Location startLocation, BulletProperties bulletProperties, boolean focused) {
            this.shooter = shooter;
            this.bulletProperties = bulletProperties;

            this.shotFrom = startLocation;
            this.bounceLocation = startLocation;
            this.lastBulletLocation = startLocation;

            double recoilRange = focused ? gun.getGunProperties().recoilRangeFocused() : gun.getGunProperties().recoilRange();
            this.currentDirection = startLocation.getDirection().normalize()
                    .add(new Vector(randomRecoil(recoilRange), randomRecoil(recoilRange), randomRecoil(recoilRange)))
                    .normalize();
        }

        private static double randomRecoil(double range) {
            return ThreadLocalRandom.current().nextDouble(-range, range);
        }

        @Override
        public void run() {
            if (currentTick++ >= 200) {
                this.cancel();
                return;
            }

            double segmentLength = (bulletProperties.speed() + (bulletProperties.speed() * (bulletProperties.speedChange() * bounces))) / 20;
            double distance = segmentLength * tickSinceBounce++;
            Vector increment = currentDirection.clone().multiply(distance);
            Location location = bounceLocation.clone().add(increment);

            spawnBulletParticle(location);

            RayTraceResult result = location.getWorld().rayTrace(location, currentDirection, segmentLength, FluidCollisionMode.NEVER, true, 0.1, entity -> entity instanceof Player player && player.getGameMode() == GameMode.ADVENTURE && player != shooter);

            if (result != null) {
                Location hitLocation = result.getHitPosition().toLocation(location.getWorld());

                spawnCenterParticle(lastBulletLocation, hitLocation);

                if (result.getHitEntity() instanceof Player hitPlayer) {
                    double shotFromDistance = hitPlayer.getLocation().distance(this.shotFrom);

                    double actualDamage = shotFromDistance > bulletProperties.effectiveDamageDist() ?
                            Math.max(bulletProperties.minDamage(), bulletProperties.damageDropOffPerBlock() * (bulletProperties.effectiveDamageDist() - shotFromDistance) + bulletProperties.maxDamage())
                            : bulletProperties.maxDamage();
                    double damage = actualDamage + (actualDamage * (bulletProperties.damageChange() * bounces));

                    hitPlayer.damage(damage, DamageSource.builder(DamageType.MOB_PROJECTILE).withCausingEntity(shooter).build());
                    hitPlayer.setNoDamageTicks(0);

                    shooter.playSound(HIT_SOUND, net.kyori.adventure.sound.Sound.Emitter.self());
                    this.cancel();

                    return;
                } else {
                    if (++bounces > bulletProperties.numberOfBounces()) {
                        this.cancel();
                        return;
                    }

                    this.bounceLocation = hitLocation;
                    this.lastBulletLocation = hitLocation;
                    this.tickSinceBounce = 0;

                    Block hitBlock = Objects.requireNonNull(result.getHitBlock());

                    Particle.BLOCK_CRACK.builder()
                            .location(hitLocation)
                            .allPlayers()
                            .force(true)
                            .count(10)
                            .extra(0.5)
                            .data(hitBlock.getBlockData())
                            .spawn();

                    net.kyori.adventure.sound.Sound hitSound = net.kyori.adventure.sound.Sound.sound(hitBlock.getBlockSoundGroup().getBreakSound().key(), Sound.Source.BLOCK, 1, 1);
                    hitLocation.getWorld().playSound(hitSound, hitLocation.getX(), hitLocation.getY(), hitLocation.getZ());

                    BlockFace blockFace = result.getHitBlockFace();

                    if (blockFace == null) {
                        return;
                    }

                    switch (blockFace) {
                        case UP, DOWN -> this.currentDirection = currentDirection.setY(-currentDirection.getY());
                        case EAST, WEST -> this.currentDirection = currentDirection.setX(-currentDirection.getX());
                        case NORTH, SOUTH -> this.currentDirection = currentDirection.setZ(-currentDirection.getZ());
                    }
                }

                return;
            }

            spawnCenterParticle(lastBulletLocation, location);
            this.lastBulletLocation = location;
        }
    }

    private void spawnCenterParticle(Location firstLocation, Location secondLocation) {
        double distance = firstLocation.distance(secondLocation);
        Location centerLocation = firstLocation.clone().add(secondLocation).multiply(0.5);

        spawnBulletParticle(centerLocation);

        if (distance < 1.5) {
            return;
        }

        spawnCenterParticle(firstLocation, centerLocation);
        spawnCenterParticle(centerLocation, secondLocation);
    }

    private void spawnBulletParticle(Location location) {
        getBulletParticleBuilder()
                .location(location)
                .allPlayers()
                .force(true)
                .spawn();
    }
}
