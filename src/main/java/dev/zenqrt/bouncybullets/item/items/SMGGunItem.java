package dev.zenqrt.bouncybullets.item.items;

import com.destroystokyo.paper.ParticleBuilder;
import dev.zenqrt.bouncybullets.game.games.Gun;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.Particle;

public final class SMGGunItem extends BulletProjectileGunItem {

    public SMGGunItem(Gun gun) {
        super("smg", Material.IRON_HORSE_ARMOR, "SMG", gun);
    }

    @Override
    protected Sound getShootingSound() {
        return Sound.sound(org.bukkit.Sound.ENTITY_ARROW_SHOOT, Sound.Source.PLAYER, 1, 2);
    }

    @Override
    protected ParticleBuilder getBulletParticleBuilder() {
        return Particle.ASH.builder();
    }
}
