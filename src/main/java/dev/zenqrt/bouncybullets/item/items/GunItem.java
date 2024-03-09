package dev.zenqrt.bouncybullets.item.items;

import dev.zenqrt.bouncybullets.BouncyBullets;
import dev.zenqrt.bouncybullets.game.games.BulletProperties;
import dev.zenqrt.bouncybullets.item.GameItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public abstract class GunItem extends GameItem {

    private final BulletProperties bulletProperties;

    public GunItem(String key, Material material, Component displayName, BulletProperties gunProperties) {
        super(key, material, displayName, buildGunPropertyDescription(gunProperties));

        this.bulletProperties = gunProperties;
    }

    @Override
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        System.out.println("Hi!");
        new ShootBulletTask(player, player.getEyeLocation()).runTaskTimer(BouncyBullets.getInstance(), 0, 1);
    }

    private static List<Component> buildGunPropertyDescription(BulletProperties gunProperties) {
        return Collections.emptyList();
    }

    private class ShootBulletTask extends BukkitRunnable {

        private final Player shooter;
        private Location bounceLocation;
        private Vector currentDirection;
        private int currentTick = 0;

        private ShootBulletTask(Player shooter, Location startLocation) {
            this.shooter = shooter;
            this.bounceLocation = startLocation;
            this.currentDirection = startLocation.getDirection().normalize();
        }

        @Override
        public void run() {
            currentTick++;

            System.out.println("Mult: " + (bulletProperties.speed() * 0.1 * currentTick));
            Vector increment = currentDirection.clone().multiply((bulletProperties.speed() / 20) * currentTick);
            Location location = bounceLocation.clone().add(increment);

            Particle.FIREWORKS_SPARK.builder()
                    .location(location)
                    .count(1)
                    .extra(0)
                    .force(true)
                    .allPlayers()
                    .spawn();

            if (location.getBlock().getType().isSolid()) {
                this.bounceLocation = location;



                this.cancel();
            }

            if (currentTick >= 200) {
                this.cancel();
            }
        }

        public BlockFace getClosestFace(float direction) {

            direction = direction % 360;

            if (direction < 0)
                direction += 360;

            direction = Math.round(direction / 45);

            return switch ((int) direction) {
                case 1 -> BlockFace.NORTH_WEST;
                case 2 -> BlockFace.NORTH;
                case 3 -> BlockFace.NORTH_EAST;
                case 4 -> BlockFace.EAST;
                case 5 -> BlockFace.SOUTH_EAST;
                case 6 -> BlockFace.SOUTH;
                case 7 -> BlockFace.SOUTH_WEST;
                default -> BlockFace.WEST;
            };
        }

        private static boolean matchBlockLocation(int blockCoordinate, double actualCoordinate) {
            return Math.abs(actualCoordinate - blockCoordinate) < 0.1;
        }

//        private static @Nullable Vector getHitDirection(Location location, double range) {
//            for (double x = 0; x < range; range+=0.05) {
//                for (double y = 0; y < range; range+=0.05) {
//                    for (double z = 0; z < range; range+=0.05) {
//                        Location currentLocation = location.clone().add(x, y, z);
//
//                        if (currentLocation.getBlock().getType().isSolid()) {
//                            return
//                        }
//                    }
//                }
//            }
//        }
    }
}
