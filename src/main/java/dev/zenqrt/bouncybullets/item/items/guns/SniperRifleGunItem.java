package dev.zenqrt.bouncybullets.item.items.guns;

import com.destroystokyo.paper.ParticleBuilder;
import dev.zenqrt.bouncybullets.loadout.gun.BulletProperties;
import dev.zenqrt.bouncybullets.loadout.gun.GunProperties;
import dev.zenqrt.bouncybullets.utils.Sounds;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Particle;

public final class SniperRifleGunItem extends BulletGunItem {

    public SniperRifleGunItem(GunProperties gunProperties, BulletProperties bulletProperties) {
        super("sniper_rifle", "Sniper Rifle", gunProperties, bulletProperties);
    }

    @Override
    protected Sound getShootingSound() {
        return Sound.sound(Sounds.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, Sound.Source.PLAYER, 1, 0);
    }

    @Override
    protected ParticleBuilder getBulletParticleBuilder() {
        return Particle.ELECTRIC_SPARK.builder()
                .extra(0);
    }
}
