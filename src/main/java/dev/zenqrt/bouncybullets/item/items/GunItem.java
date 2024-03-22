package dev.zenqrt.bouncybullets.item.items;

import com.destroystokyo.paper.ParticleBuilder;
import dev.zenqrt.bouncybullets.BouncyBullets;
import dev.zenqrt.bouncybullets.game.games.BulletProperties;
import dev.zenqrt.bouncybullets.item.GameItem;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class GunItem extends GameItem {

    private final BulletProperties bulletProperties;

    public GunItem(String key, Material material, Component displayName, BulletProperties gunProperties) {
        super(key, material, displayName, buildGunPropertyDescription(gunProperties));

        this.bulletProperties = gunProperties;
    }

    protected abstract Sound getShootingSound();
    protected abstract ParticleBuilder getBulletParticleBuilder();

    @Override
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        player.getWorld().playSound(getShootingSound(), player.getX(), player.getY(), player.getZ());
        new ShootBulletTask(player, player.getEyeLocation()).runTaskTimer(BouncyBullets.getInstance(), 0, 1);
    }

    private static List<Component> buildGunPropertyDescription(BulletProperties gunProperties) {
        return Collections.emptyList();
    }

    private class ShootBulletTask extends BukkitRunnable {

        private final Player shooter;
        private Location bounceLocation;
        private Location lastBulletLocation;
        private Vector currentDirection;
        private int bounces = 0;
        private int tickSinceBounce = 0;
        private int currentTick = 0;

        private ShootBulletTask(Player shooter, Location startLocation) {
            this.shooter = shooter;
            this.bounceLocation = startLocation;
            this.lastBulletLocation = startLocation;
            this.currentDirection = startLocation.getDirection().normalize();
        }

        @Override
        public void run() {
            if (currentTick++ >= 200) {
                this.cancel();
                return;
            }

            tickSinceBounce++;

            double segmentLength = (bulletProperties.speed() + (bulletProperties.speed() * (bulletProperties.speedChange() * bounces))) / 20;
            double distance = segmentLength * tickSinceBounce;
            Vector increment = currentDirection.clone().multiply(distance);
            Location location = bounceLocation.clone().add(increment);

            spawnBulletParticle(location);

            RayTraceResult entityResult = location.getWorld().rayTraceEntities(location, currentDirection.clone().multiply(-1), segmentLength, 0.1, entity -> entity instanceof Player player && player.getGameMode() == GameMode.ADVENTURE && entity != shooter);

            if (entityResult != null) {
                LivingEntity hitEntity = Objects.requireNonNull((LivingEntity) entityResult.getHitEntity());
                double damage = bulletProperties.damage() + (bulletProperties.damage() * (bulletProperties.damageChange() * bounces));

                hitEntity.damage(damage, shooter);
                hitEntity.setNoDamageTicks(0);

                Location hitLocation = entityResult.getHitPosition().toLocation(location.getWorld());
                shooter.playSound(Sound.sound(org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP.key(), Sound.Source.PLAYER, 1, 2), Sound.Emitter.self());
                spawnCenterParticle(lastBulletLocation, hitLocation);

                this.cancel();
                return;
            }

            RayTraceResult result = location.getWorld().rayTraceBlocks(bounceLocation, currentDirection, distance, FluidCollisionMode.NEVER, true);

            if (result != null) {
                if (++bounces > bulletProperties.numberOfBounces()) {
                    this.cancel();
                    return;
                }

                this.bounceLocation = result.getHitPosition().toLocation(location.getWorld());
                spawnCenterParticle(lastBulletLocation, bounceLocation);

                this.lastBulletLocation = bounceLocation;
                this.tickSinceBounce = 0;
                BlockFace blockFace = result.getHitBlockFace();

                if (blockFace == null) {
                    return;
                }

                switch (blockFace) {
                    case UP, DOWN -> this.currentDirection = currentDirection.setY(-currentDirection.getY());
                    case EAST, WEST -> this.currentDirection = currentDirection.setX(-currentDirection.getX());
                    case NORTH, SOUTH -> this.currentDirection = currentDirection.setZ(-currentDirection.getZ());
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
