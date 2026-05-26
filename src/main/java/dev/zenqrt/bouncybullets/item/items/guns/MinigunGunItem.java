package dev.zenqrt.bouncybullets.item.items.guns;

import com.destroystokyo.paper.ParticleBuilder;
import dev.zenqrt.bouncybullets.loadout.gun.BulletProperties;
import dev.zenqrt.bouncybullets.loadout.gun.GunProperties;
import dev.zenqrt.bouncybullets.utils.Sounds;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.Particle;

public final class MinigunGunItem extends BulletGunItem {

    public MinigunGunItem(GunProperties gunProperties, BulletProperties bulletProperties) {
        super(
                "minigun",
                Material.GOLDEN_HORSE_ARMOR,
                "Minigun",
                gunProperties,
                bulletProperties
        );
    }

    @Override
    protected Sound getShootingSound() {
        return Sound.sound(Sounds.ENTITY_IRON_GOLEM_HURT, Sound.Source.PLAYER, 1, 2);
    }

    @Override
    protected ParticleBuilder getBulletParticleBuilder() {
        return Particle.CRIT.builder()
                .count(1)
                .extra(0);
    }
}
