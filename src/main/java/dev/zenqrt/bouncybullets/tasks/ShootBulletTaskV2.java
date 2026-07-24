package dev.zenqrt.bouncybullets.tasks;

import com.destroystokyo.paper.ParticleBuilder;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import dev.zenqrt.bouncybullets.loadout.gun.BulletProperties;
import dev.zenqrt.bouncybullets.player.PlayerSessionManager;
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

import java.util.Optional;
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
    private Vector currentDirection;
    private int particleIndex;
    private int bounces;
    private int currentTick;

    private final BulletProperties bulletProperties;
    private final BouncyBulletGame game;
    private final BouncyBulletGamePlayer shooter;

    public ShootBulletTaskV2(BouncyBulletGamePlayer shooter, BouncyBulletGame game, BulletProperties bulletProperties, Location shotFrom, Vector direction, double spread) {
        this.shooter = shooter;
        this.game = game;
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

        Location current = this.bulletLocation.clone();
        Vector direction = this.currentDirection.clone();
        Location lastBounceInTick = this.lastBounceLocation.clone();
        double remainingDistance = this.bulletProperties.speed() / 20;
        int bouncesInTick = 0;

        while (remainingDistance > 0) {
            World world = current.getWorld();
            Location tail = current.clone();

            current.add(
                    direction.clone()
                            .normalize()
                            .multiply(remainingDistance)
            );

            // Find collisions
            RayTraceResult result = raytraceBullet(tail, direction, remainingDistance);

            if (result == null) {
                spawnBulletParticle(tail, direction, remainingDistance);
                break;
            }

            Location hitLocation = result.getHitPosition().toLocation(world);
            double distanceTravelled = tail.distance(hitLocation);

            spawnBulletParticle(tail, direction, distanceTravelled);

            // Handle player damage
            if (result.getHitEntity() instanceof Player hitPlayer) {
                Optional<BouncyBulletGamePlayer> gamePlayerOptional = this.game.findPlayer(hitPlayer.getUniqueId());

                if (gamePlayerOptional.isEmpty())
                    return;

                double lastBounceDistance = hitPlayer.getLocation().distance(lastBounceInTick);
                double damage = calculateBulletDamage(lastBounceDistance);

                BouncyBulletGamePlayer gamePlayer = gamePlayerOptional.get();

                gamePlayer.hurt(
                        (int) damage,
                        this.shooter
                );

                HIT_PARTICLE
                        .location(hitLocation)
                        .spawn();

                this.shooter.getPlayer().playSound(HIT_SOUND, Sound.Emitter.self());

                this.cancel();
                return;
            }

            Block hitBlock = result.getHitBlock();

            if (hitBlock == null)
                break;

            if (this.bounces + bouncesInTick >= this.bulletProperties.numberOfBounces()) {
                this.cancel();
                return;
            }

            // Handle block collision and bouncing
            current = hitLocation;
            lastBounceInTick = hitLocation;
            bouncesInTick++;

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
                break;

            switch (blockFace) {
                case UP, DOWN -> direction.setY(-direction.getY());
                case EAST, WEST -> direction.setX(-direction.getX());
                case NORTH, SOUTH -> direction.setZ(-direction.getZ());
            }

            remainingDistance -= distanceTravelled;
        }

        this.bulletLocation = current;
        this.currentDirection = direction;
        this.lastBounceLocation = lastBounceInTick;
        this.bounces += bouncesInTick;
    }

    private RayTraceResult raytraceBullet(Location start, Vector direction, double distance) {
        return start.getWorld().rayTrace(builder ->
                builder
                        .start(start)
                        .maxDistance(distance)
                        .direction(direction)
                        .fluidCollisionMode(FluidCollisionMode.NEVER)
                        .raySize(0.1)
                        .ignorePassableBlocks(true)
                        .entityFilter(entity -> entity instanceof Player player
                                && player.getGameMode() == GameMode.ADVENTURE
                                && (player != this.shooter || this.bounces > 0)
                        )
                        .targets(RayTraceTarget.BLOCK, RayTraceTarget.ENTITY)
        );
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

    private void spawnBulletParticle(Location start, Vector direction, double distance) {
        int cycleLength = (int) ((this.bulletProperties.speed() / 20) / TRAIL_PARTICLE_STEP);

        Location location = start.clone();

        Vector directionStep = direction.clone()
                .normalize()
                .multiply(TRAIL_PARTICLE_STEP);

        for (double currentDistance = 0; currentDistance <= distance; currentDistance += TRAIL_PARTICLE_STEP) {
            int index = this.particleIndex % cycleLength;

            ParticleBuilder particleBuilder = index < cycleLength - BULLET_PARTICLE_AMOUNT
                    ? Particle.CRIT.builder()
                        .extra(0)
                    : Particle.END_ROD.builder()
                        .extra(1000)
                        .count(1);

            particleBuilder
                    .location(location)
                    .spawn();

            location.add(directionStep);
            this.particleIndex++;
        }
    }
}
