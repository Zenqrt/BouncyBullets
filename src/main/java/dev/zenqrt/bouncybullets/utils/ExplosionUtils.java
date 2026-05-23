package dev.zenqrt.bouncybullets.utils;

import net.kyori.adventure.sound.Sound;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.damage.DamageSource;
import org.bukkit.util.Vector;

public final class ExplosionUtils {

    public static void createExplosion(Location location, double radius, double damage, DamageSource damageSource) {
        Particle.EXPLOSION.builder()
                .force(true)
                .allPlayers()
                .count(1)
                .location(location)
                .spawn();

        location.getWorld().playSound(Sound.sound(org.bukkit.Sound.ENTITY_GENERIC_EXPLODE, Sound.Source.PLAYER, 1, 0.75F), location.getX(), location.getY(), location.getZ());
        location.getNearbyPlayers(radius).forEach(player -> player.damage(calculateDamage(damage, player.getLocation(), location), damageSource));
    }


    private static double calculateDamage(double originalDamage, Location playerLocation, Location explosionLocation) {
        int isBehindBlock = 0;

        if (hasBlockInBetween(playerLocation, explosionLocation)) {
            isBehindBlock++;
        }

        if (hasBlockInBetween(playerLocation.clone().add(0, 1, 0), explosionLocation)) {
            isBehindBlock++;
        }

        if (isBehindBlock == 2) {
            return 0;
        } else if (isBehindBlock == 1) {
            return originalDamage * 0.5;
        }

        return originalDamage;
    }

    private static boolean hasBlockInBetween(Location firstLocation, Location secondLocation) {
        Vector direction = secondLocation.toVector().subtract(firstLocation.toVector());
        return firstLocation.getWorld().rayTraceBlocks(firstLocation, direction.normalize(), direction.length(), FluidCollisionMode.NEVER) != null;
    }
}
