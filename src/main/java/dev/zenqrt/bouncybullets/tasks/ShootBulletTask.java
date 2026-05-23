package dev.zenqrt.bouncybullets.tasks;

import com.destroystokyo.paper.ParticleBuilder;
import dev.zenqrt.bouncybullets.loadout.gun.BulletProperties;
import dev.zenqrt.bouncybullets.utils.Sounds;
import net.kyori.adventure.sound.Sound;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public final class ShootBulletTask extends BukkitRunnable {

    private static final Sound HIT_SOUND = Sound.sound(Sounds.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.PLAYER, 1, 2);

    private final Player shooter;
    private final BulletProperties bulletProperties;
    private final ParticleBuilder trailParticle;
    private Location bounceLocation;
    private Location lastBulletLocation;
    private Vector currentDirection;
    private int bounces = 0;
    private int tickSinceBounce = 0;
    private int currentTick = 0;

    public ShootBulletTask(Player shooter, Location startLocation, Vector direction, BulletProperties bulletProperties, double recoilRange, ParticleBuilder trailParticle) {
        this.shooter = shooter;
        this.bulletProperties = bulletProperties;
        this.trailParticle = trailParticle;

        this.bounceLocation = startLocation;
        this.lastBulletLocation = startLocation;

        if (recoilRange == 0)
            this.currentDirection = direction;
        else
            this.currentDirection = direction
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
                double shotFromDistance = hitPlayer.getLocation().distance(this.bounceLocation);

                double actualDamage = shotFromDistance > bulletProperties.effectiveDamageDist() ?
                        Math.max(bulletProperties.minDamage(), bulletProperties.damageDropOffPerBlock() * (bulletProperties.effectiveDamageDist() - shotFromDistance) + bulletProperties.maxDamage())
                        : bulletProperties.maxDamage();
                double damage = actualDamage + (actualDamage * (bulletProperties.damageChange() * bounces));

                hitPlayer.damage(damage, DamageSource.builder(DamageType.MOB_PROJECTILE).withCausingEntity(shooter).build());
                hitPlayer.setNoDamageTicks(0);

                shooter.playSound(HIT_SOUND, Sound.Emitter.self());
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

                Particle.BLOCK_CRUMBLE.builder()
                        .location(hitLocation)
                        .allPlayers()
                        .force(true)
                        .count(10)
                        .extra(0.5)
                        .data(hitBlock.getBlockData())
                        .spawn();

                Sound hitSound = Sound.sound(hitBlock.getBlockSoundGroup().getBreakSound(), Sound.Source.BLOCK, 1, 1);
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
        this.trailParticle
                .location(location)
                .allPlayers()
                .force(true)
                .spawn();
    }
}
