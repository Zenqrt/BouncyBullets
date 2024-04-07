package dev.zenqrt.bouncybullets.item.items;

import com.destroystokyo.paper.ParticleBuilder;
import dev.zenqrt.bouncybullets.game.games.Gun;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.Particle;

public final class SilencedPistolGunItem extends BulletProjectileGunItem {

    public SilencedPistolGunItem(Gun gun) {
        super("silenced_pistol", Material.IRON_HOE, "Silenced Pistol", gun);
    }

    @Override
    protected Sound getShootingSound() {
        return Sound.sound(org.bukkit.Sound.ENTITY_FIREWORK_ROCKET_BLAST, Sound.Source.PLAYER, 0.5F, 2);
    }

    @Override
    protected ParticleBuilder getBulletParticleBuilder() {
        return Particle.CRIT.builder()
                .extra(0.01);
    }
}
