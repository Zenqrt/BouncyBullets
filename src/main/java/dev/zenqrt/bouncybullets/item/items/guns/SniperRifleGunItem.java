package dev.zenqrt.bouncybullets.item.items.guns;

import com.destroystokyo.paper.ParticleBuilder;
import dev.zenqrt.bouncybullets.loadout.gun.BulletProperties;
import dev.zenqrt.bouncybullets.loadout.gun.GunProperties;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.Particle;

public final class SniperRifleGunItem extends BulletProjectileGunItem {

    public SniperRifleGunItem(GunProperties gunProperties, BulletProperties bulletProperties) {
        super("sniper_rifle", Material.DIAMOND_HORSE_ARMOR, "Sniper Rifle", gunProperties, bulletProperties);
    }

    @Override
    protected Sound getShootingSound() {
        return Sound.sound(org.bukkit.Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, Sound.Source.PLAYER, 1, 0);
    }

    @Override
    protected ParticleBuilder getBulletParticleBuilder() {
        return Particle.ELECTRIC_SPARK.builder()
                .extra(0);
    }
}
