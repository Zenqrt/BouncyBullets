package dev.zenqrt.bouncybullets.tasks;

import com.destroystokyo.paper.ParticleBuilder;
import dev.zenqrt.bouncybullets.loadout.gun.BulletProperties;
import dev.zenqrt.bouncybullets.utils.Sounds;
import io.papermc.paper.raytracing.RayTraceTarget;
import net.kyori.adventure.sound.Sound;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public final class ShootBulletTaskV2 extends BukkitRunnable {

    private static final int MAX_BULLET_ALIVE_TICKS = 200;
    private static final int BULLET_PARTICLE_AMOUNT = 5;
    private static final double TRAIL_PARTICLE_STEP = 0.25;
    private static final Sound HIT_SOUND = Sound.sound(Sounds.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.PLAYER, 1, 2);
    private static final ParticleBuilder HIT_PARTICLE = Particle.DAMAGE_INDICATOR.builder()
            .extra(0)
            .count(1);

    private Location lastBounceLocation;
    private Location bulletLocation;
    private final  Vector currentDirection;
    private int bounces;
    private int currentTick;

    private final BulletProperties bulletProperties;
    private final Player shooter;

    public ShootBulletTaskV2(Player shooter, BulletProperties bulletProperties, Location shotFrom, Vector direction, double spread) {
        this.shooter = shooter;
        this.bulletProperties = bulletProperties;
        this.bulletLocation = shotFrom.clone();
        this.lastBounceLocation = shotFrom.clone();

        double segmentLength = bulletProperties.speed() / 20;

        if (spread <= 0)
            this.currentDirection = direction
                    .normalize()
                    .multiply(segmentLength);
        else
            this.currentDirection = direction
                    .add(new Vector(
                            randomSpread(spread),
                            randomSpread(spread),
                            randomSpread(spread)
                    ))
                    .normalize()
                    .multiply(segmentLength);
    }

    private static double randomSpread(double range) {
        return ThreadLocalRandom.current().nextDouble(-range, range);
    }

    @Override
    public void run() {
        if (this.currentTick++ >= MAX_BULLET_ALIVE_TICKS || this.bounces > this.bulletProperties.numberOfBounces()) {
            this.cancel();
            return;
        }

        Location tail = this.bulletLocation.clone();
        Location head = this.bulletLocation
                .add(this.currentDirection);
        World world = head.getWorld();
        double distance = head.distance(tail);

//        for (int i = 0; i < particles - 3; i++) {
//            Particle.CRIT.builder()
//                    .extra(0)
//                    .location(tail)
//                    .allPlayers()
//                    .force(true)
//                    .spawn();
//
//            tail.add(
//                    this.currentDirection.clone()
//                            .normalize()
//                            .multiply(TRAIL_PARTICLE_STEP)
//            );
//        }
//
//        for (int i = 0; i < 3; i++) {
//            Particle.END_ROD.builder()
//                    .location(tail)
//                    .extra(1000)
//                    .count(1)
//                    .spawn();
//
//            tail.add(
//                    this.currentDirection.clone()
//                            .normalize()
//                            .multiply(TRAIL_PARTICLE_STEP)
//            );
//        }

        // Find collisions
        RayTraceResult result = world.rayTrace(builder ->
                builder
                        .start(tail)
                        .maxDistance(distance)
                        .direction(this.currentDirection)
                        .fluidCollisionMode(FluidCollisionMode.NEVER)
                        .raySize(0.1)
                        .ignorePassableBlocks(true)
                        .entityFilter(entity -> entity instanceof Player player
                                && player.getGameMode() == GameMode.ADVENTURE
                                && (player != this.shooter || this.bounces > 0)
                        )
                        .targets(RayTraceTarget.BLOCK, RayTraceTarget.ENTITY)
        );

        if (result == null) {
            // Spawn particles
            int particles = (int) (distance / TRAIL_PARTICLE_STEP);

            Location endLocation = spawnParticleLine(
                    tail,
                    this.currentDirection,
                    particles - BULLET_PARTICLE_AMOUNT,
                    Particle.CRIT.builder()
                            .extra(0)
            );

            spawnParticleLine(
                    endLocation,
                    this.currentDirection,
                    BULLET_PARTICLE_AMOUNT,
                    Particle.END_ROD.builder()
                            .extra(1000)
                            .count(1)
            );

            return;
        }

        Location hitLocation = result.getHitPosition().toLocation(world);

        // TODO: Make particle calculations distance based instead of size based
        // TODO: Also, particle indexing would be must easier than whatever the hell I have been doing
        int maxParticles = (int) (distance / TRAIL_PARTICLE_STEP);
        int particles = (int) (tail.distance(hitLocation) / TRAIL_PARTICLE_STEP);

        int bulletParticles = BULLET_PARTICLE_AMOUNT - (maxParticles - particles);

        Location endLocation = spawnParticleLine(
                tail,
                this.currentDirection,
                particles - bulletParticles,
                Particle.CRIT.builder()
                        .extra(0)
        );

        if (bulletParticles != 0) {
            spawnParticleLine(
                    endLocation,
                    this.currentDirection,
                    bulletParticles,
                    Particle.END_ROD.builder()
                            .extra(1000)
                            .count(1)
            );
        }

        if (result.getHitEntity() instanceof Player hitPlayer) {
            double lastBounceDistance = hitPlayer.getLocation().distance(this.lastBounceLocation);
            double damage = calculateBulletDamage(lastBounceDistance);

            hitPlayer.damage(
                    damage,
                    DamageSource.builder(DamageType.MOB_PROJECTILE)
                            .withDirectEntity(this.shooter)
                            .withCausingEntity(this.shooter)
                            .build()
            );
            hitPlayer.setNoDamageTicks(0);

            HIT_PARTICLE
                    .location(hitLocation)
                    .spawn();

            this.shooter.playSound(HIT_SOUND, Sound.Emitter.self());

            this.cancel();
            return;
        }

        Block hitBlock = result.getHitBlock();

        if (hitBlock != null) {
            this.lastBounceLocation = hitLocation;
            this.bounces++;

            Particle.BLOCK_CRUMBLE.builder()
                    .location(result.getHitPosition().toLocation(world))
                    .allPlayers()
                    .force(true)
                    .count(10)
                    .extra(0.5)
                    .data(hitBlock.getBlockData())
                    .spawn();

            Sound hitSound = Sound.sound(
                    hitBlock.getBlockSoundGroup().getBreakSound(),
                    Sound.Source.BLOCK,
                    1,
                    1
            );
            world.playSound(hitSound, hitLocation.x(), hitLocation.y(), hitLocation.z());

            BlockFace blockFace = result.getHitBlockFace();

            if (blockFace == null)
                return;

            switch (blockFace) {
                case UP, DOWN -> this.currentDirection.setY(-this.currentDirection.getY());
                case EAST, WEST -> this.currentDirection.setX(-this.currentDirection.getX());
                case NORTH, SOUTH -> this.currentDirection.setZ(-this.currentDirection.getZ());
            }

            int remainingParticles = maxParticles - particles;

            if (remainingParticles > 0) {
                if (remainingParticles > BULLET_PARTICLE_AMOUNT) {
                    int trailParticles = remainingParticles - BULLET_PARTICLE_AMOUNT;

                    endLocation = spawnParticleLine(
                            hitLocation,
                            this.currentDirection,
                            trailParticles,
                            Particle.CRIT.builder()
                                    .extra(0)
                    );

                    endLocation = spawnParticleLine(
                            endLocation,
                            this.currentDirection,
                            BULLET_PARTICLE_AMOUNT,
                            Particle.END_ROD.builder()
                                    .extra(1000)
                                    .count(1)
                    );
                } else {
                    endLocation = spawnParticleLine(
                            hitLocation,
                            this.currentDirection,
                            remainingParticles,
                            Particle.END_ROD.builder()
                                    .extra(1000)
                                    .count(1)
                    );
                }
            }

            this.bulletLocation = endLocation;
        }

    }

    private double calculateBulletDamage(double lastBounceDistance) {
        double damage = this.bulletProperties.maxDamage();

        if (lastBounceDistance > this.bulletProperties.effectiveDamageDist()) {
            double distancePastEffectiveRange = lastBounceDistance - this.bulletProperties.effectiveDamageDist();
            double dropOff = this.bulletProperties.damageDropOffPerBlock() * distancePastEffectiveRange;

            damage = Math.max(
                    this.bulletProperties.minDamage(),
                    this.bulletProperties.maxDamage() - dropOff
            );
        }

        double bounceScale = 1 + this.bulletProperties.damageChange() * this.bounces;

        return damage * bounceScale;
    }

    private static Location spawnParticleLine(Location start, Vector direction, int times, ParticleBuilder builder) {
        Location location = start.clone();
        Vector directionStep = direction.clone()
                .normalize()
                .multiply(TRAIL_PARTICLE_STEP);

        for (int i = 0; i < times; i++) {
            builder.location(location)
                    .allPlayers()
                    .force(true)
                    .spawn();

            location.add(directionStep);
        }

        return location;
    }
}
