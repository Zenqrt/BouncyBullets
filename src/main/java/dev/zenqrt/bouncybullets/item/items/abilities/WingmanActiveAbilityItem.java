package dev.zenqrt.bouncybullets.item.items.abilities;

import com.destroystokyo.paper.ParticleBuilder;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.loadout.gun.BulletProperties;
import dev.zenqrt.bouncybullets.tasks.ShootBulletTask;
import dev.zenqrt.bouncybullets.utils.MiniMessageUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public final class WingmanActiveAbilityItem extends ActiveAbilityItem {

    private static final int COOLDOWN_TICKS = 1200;
    private static final BulletProperties BULLET_PROPERTIES = new BulletProperties(
            0,
            300,
            0.1F,
            5,
            5,
            0.1F,
            20,
            5
    );
    private static final Sound SHOOT_SOUND = Sound.sound(Key.key("entity.iron_golem.hurt"), Sound.Source.PLAYER, 1, 2);

    public WingmanActiveAbilityItem() {
        super(
                "wingman_active_ability",
                Material.FIREWORK_STAR,
                "Bullet Spread",
                MiniMessageUtils.wordWrapLore(
                        List.of(
                                "<gray>Fire a wave of bullets around you, dealing <red>" + BULLET_PROPERTIES.maxDamage() + "❤<gray> damage per bullet.",
                                "",
                                "<dark_gray>Cooldown: <green>" + (COOLDOWN_TICKS / 20) + "s"
                        ),
                        30
                )
        );
    }

    @Override
    public void onUse(BouncyBulletGame game, Player player, ItemStack itemStack, PlayerInteractEvent event) {
        new ShootingTask(game.getPlugin(), player)
                .runTaskTimer(game.getPlugin(), 0, 0);

        player.setCooldown(super.material, COOLDOWN_TICKS);
    }

    private static ParticleBuilder createBulletTrail() {
        return Particle.ELECTRIC_SPARK.builder()
                .count(1)
                .extra(0)
                .force(true);
    }

    private static class ShootingTask extends BukkitRunnable {

        private static final int SHOTS_PER_ITERATION = 4;
        private double thetaOffset;
        private final Player shooter;
        private final Plugin plugin;

        ShootingTask(Plugin plugin, Player shooter) {
            this.plugin = plugin;
            this.shooter = shooter;
            this.thetaOffset = 0;
        }

        @Override
        public void run() {
            this.shooter.getWorld().playSound(SHOOT_SOUND, this.shooter);

            for (int i = 0; i < SHOTS_PER_ITERATION; i++) {
                Vector direction = new Vector(
                        Math.cos(this.thetaOffset),
                        0,
                        Math.sin(this.thetaOffset)
                );

                new ShootBulletTask(
                        this.shooter,
                        this.shooter.getEyeLocation(),
                        direction,
                        BULLET_PROPERTIES,
                        0,
                        createBulletTrail()
                ).runTaskTimer(this.plugin, 1, 1);

                if (this.thetaOffset >= 2 * Math.PI) {
                    this.cancel();
                    return;
                }

                this.thetaOffset += Math.PI / 16;
            }
        }
    }
}
