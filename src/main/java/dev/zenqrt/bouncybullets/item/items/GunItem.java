package dev.zenqrt.bouncybullets.item.items;

import com.destroystokyo.paper.ParticleBuilder;
import dev.zenqrt.bouncybullets.BouncyBullets;
import dev.zenqrt.bouncybullets.game.event.impl.PaperEventListener;
import dev.zenqrt.bouncybullets.game.games.BulletProperties;
import dev.zenqrt.bouncybullets.game.games.Gun;
import dev.zenqrt.bouncybullets.item.GameItem;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public abstract class GunItem extends GameItem {

    private static final Sound HIT_SOUND = Sound.sound(org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP.key(), Sound.Source.PLAYER, 1, 2);

    private final Gun gun;
    private final Map<UUID, Long> lastShootTimes = new HashMap<>();

    public GunItem(String key, Material material, Component displayName, Gun gun) {
        super(key, material, displayName, buildGunPropertyDescription(gun));

        this.gun = gun;
    }

    protected abstract Sound getShootingSound();
    protected abstract ParticleBuilder getBulletParticleBuilder();

    @Override
    public void registerEvents() {
        this.eventNode.registerListener(PaperEventListener.builder(PlayerInteractEvent.class)
                .filter(event -> filterGameItem(event.getItem(), this))
                .filter(event -> event.getAction().isRightClick())
                .handler(event -> {
                    Player player = event.getPlayer();

                    if (player.getGameMode() != GameMode.ADVENTURE) {
                        return;
                    }

                    if (lastShootTimes.containsKey(player.getUniqueId())) {
                        long lastShootTime = lastShootTimes.get(player.getUniqueId());

                        if (System.currentTimeMillis() - lastShootTime < gun.getGunProperties().shootDelayMillis()) {
                            return;
                        }

                        lastShootTimes.put(player.getUniqueId(), System.currentTimeMillis());
                    } else {
                        lastShootTimes.put(player.getUniqueId(), System.currentTimeMillis());
                    }

                    player.getWorld().playSound(getShootingSound(), player.getX(), player.getY(), player.getZ());
                    new ShootBulletTask(player, player.getEyeLocation(), player.hasPotionEffect(PotionEffectType.SLOW)).runTaskTimer(BouncyBullets.getInstance(), 0, 1);
                })
                .build());
        this.eventNode.registerListener(PaperEventListener.builder(PlayerInteractEvent.class)
                .filter(event -> filterGameItem(event.getItem(), this))
                .filter(event -> event.getAction().isLeftClick())
                .handler(event -> {
                    event.setCancelled(true);

                    Player player = event.getPlayer();

                    if (player.hasPotionEffect(PotionEffectType.SLOW)) {
                        player.removePotionEffect(PotionEffectType.SLOW);
                    } else {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 2, false, false, false));
                    }
                })
                .build());
        this.eventNode.registerListener(PaperEventListener.builder(PlayerSwapHandItemsEvent.class)
                .filter(event -> filterGameItem(event.getOffHandItem(), this))
                .handler(event -> {
                    event.setCancelled(true);

                    Player player = event.getPlayer();
                    player.sendMessage(Component.text("I'm supposed to reload but I can't do that yet!", NamedTextColor.GOLD));
                })
                .build());
    }

    private static List<Component> buildGunPropertyDescription(Gun gun) {
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

        private ShootBulletTask(Player shooter, Location startLocation, boolean focused) {
            this.shooter = shooter;

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

            BulletProperties bulletProperties = gun.getBulletProperties();

            double segmentLength = (bulletProperties.speed() + (bulletProperties.speed() * (bulletProperties.speedChange() * bounces))) / 20;
            double distance = segmentLength * tickSinceBounce++;
            Vector increment = currentDirection.clone().multiply(distance);
            Location location = bounceLocation.clone().add(increment);

            spawnBulletParticle(location);

            RayTraceResult result = location.getWorld().rayTrace(location, currentDirection, segmentLength, FluidCollisionMode.NEVER, true, 0.1, entity -> entity instanceof Player player && player.getGameMode() == GameMode.ADVENTURE && player != shooter);

            if (result != null) {
                Location hitLocation = result.getHitPosition().toLocation(location.getWorld());

                if (result.getHitEntity() instanceof Player hitPlayer) {
                    double damage = bulletProperties.damage() + (bulletProperties.damage() * (bulletProperties.damageChange() * bounces));

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

                spawnCenterParticle(lastBulletLocation, hitLocation);
                return;
            }

//            RayTraceResult blockResult = location.getWorld().rayTraceBlocks(location, currentDirection, segmentLength, FluidCollisionMode.NEVER, true);
//
//            if (blockResult != null) {
//                if (++bounces > bulletProperties.numberOfBounces()) {
//                    this.cancel();
//                    return;
//                }
//
//                this.bounceLocation = blockResult.getHitPosition().toLocation(location.getWorld());
//                spawnCenterParticle(lastBulletLocation, bounceLocation);
//
//                this.lastBulletLocation = bounceLocation;
//                this.tickSinceBounce = 0;
//                BlockFace blockFace = blockResult.getHitBlockFace();
//
//                if (blockFace == null) {
//                    return;
//                }
//
//                switch (blockFace) {
//                    case UP, DOWN -> this.currentDirection = currentDirection.setY(-currentDirection.getY());
//                    case EAST, WEST -> this.currentDirection = currentDirection.setX(-currentDirection.getX());
//                    case NORTH, SOUTH -> this.currentDirection = currentDirection.setZ(-currentDirection.getZ());
//                }
//
//                return;
//            }
//
//            RayTraceResult entityResult = location.getWorld().rayTraceEntities(location, currentDirection.clone(), segmentLength, 0.1, entity -> entity instanceof Player player && player.getGameMode() == GameMode.ADVENTURE && entity != shooter);
//
//            if (entityResult != null) {
//                LivingEntity hitEntity = Objects.requireNonNull((LivingEntity) entityResult.getHitEntity());
//                double damage = bulletProperties.damage() + (bulletProperties.damage() * (bulletProperties.damageChange() * bounces));
//
//                hitEntity.damage(damage, DamageSource.builder(DamageType.MOB_PROJECTILE).withCausingEntity(shooter).build());
//                hitEntity.setNoDamageTicks(0);
//
//                Location hitLocation = entityResult.getHitPosition().toLocation(location.getWorld());
//                shooter.playSound(Sound.sound(org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP.key(), Sound.Source.PLAYER, 1, 2), Sound.Emitter.self());
//                spawnCenterParticle(lastBulletLocation, hitLocation);
//
//                this.cancel();
//                return;
//            }

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
