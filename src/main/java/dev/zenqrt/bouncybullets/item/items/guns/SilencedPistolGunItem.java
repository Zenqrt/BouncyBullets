package dev.zenqrt.bouncybullets.item.items.guns;

import com.destroystokyo.paper.ParticleBuilder;
import dev.zenqrt.bouncybullets.loadout.gun.BulletProperties;
import dev.zenqrt.bouncybullets.loadout.gun.GunProperties;
import dev.zenqrt.bouncybullets.utils.Sounds;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Particle;

public final class SilencedPistolGunItem extends BulletGunItem {

    public SilencedPistolGunItem(GunProperties gunProperties, BulletProperties bulletProperties, TipOffset tipOffset, TipOffset tipOffsetAiming) {
        super(
                "silenced_pistol",
                "Silenced Pistol",
                gunProperties,
                bulletProperties,
                tipOffset,
                tipOffsetAiming
        );
    }

    @Override
    protected Sound getShootingSound() {
        return Sound.sound(Sounds.ENTITY_FIREWORK_ROCKET_BLAST, Sound.Source.PLAYER, 0.5F, 2);
    }

    @Override
    protected ParticleBuilder getBulletParticleBuilder() {
        return Particle.CRIT.builder()
                .extra(0.01);
    }
}
