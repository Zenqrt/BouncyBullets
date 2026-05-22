package dev.zenqrt.bouncybullets.item.items.guns;

import com.destroystokyo.paper.ParticleBuilder;
import dev.zenqrt.bouncybullets.loadout.gun.BulletProperties;
import dev.zenqrt.bouncybullets.loadout.gun.GunProperties;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.Particle;

public final class SpecialBurstRifleGunItem extends BurstBulletGunItem {

    public SpecialBurstRifleGunItem(GunProperties gunProperties, BulletProperties bulletProperties, int burstRounds) {
        super(
                "special_burst_rifle",
                Material.DIAMOND_HORSE_ARMOR,
                "Burst Rifle",
                gunProperties,
                bulletProperties,
                burstRounds
        );
    }

    @Override
    protected Sound getShootingSound() {
        return Sound.sound(Key.key("entity.arrow.shoot"), Sound.Source.PLAYER, 1, 2);
    }

    @Override
    protected ParticleBuilder getBulletParticleBuilder() {
        return Particle.ASH.builder();
    }
}
