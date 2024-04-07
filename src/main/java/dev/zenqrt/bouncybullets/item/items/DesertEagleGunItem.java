package dev.zenqrt.bouncybullets.item.items;

import com.destroystokyo.paper.ParticleBuilder;
import dev.zenqrt.bouncybullets.game.games.Gun;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.Particle;

public final class DesertEagleGunItem extends BulletProjectileGunItem {

    public DesertEagleGunItem(Gun gun) {
        super("desert_eagle", Material.DIAMOND_HOE, "Desert Eagle", gun);
    }

    @Override
    protected ParticleBuilder getBulletParticleBuilder() {
        return Particle.CRIT_MAGIC.builder()
                .count(1)
                .extra(0);
    }

    @Override
    protected Sound getShootingSound() {
        return Sound.sound(org.bukkit.Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, Sound.Source.PLAYER, 1, 1);
    }
}
