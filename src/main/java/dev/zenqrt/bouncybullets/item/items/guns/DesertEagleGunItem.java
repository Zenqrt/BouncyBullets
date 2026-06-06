package dev.zenqrt.bouncybullets.item.items.guns;

import com.destroystokyo.paper.ParticleBuilder;
import dev.zenqrt.bouncybullets.loadout.gun.BulletProperties;
import dev.zenqrt.bouncybullets.loadout.gun.GunProperties;
import dev.zenqrt.bouncybullets.utils.Sounds;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Particle;

public final class DesertEagleGunItem extends BulletGunItem {

    public DesertEagleGunItem(GunProperties gunProperties, BulletProperties bulletProperties, TipOffset tipOffset, TipOffset tipOffsetAiming) {
        super(
                "desert_eagle", 
                "Desert Eagle",
                gunProperties,
                bulletProperties,
                tipOffset,
                tipOffsetAiming
        );
    }

    @Override
    protected ParticleBuilder getBulletParticleBuilder() {
        return Particle.ENCHANTED_HIT.builder()
                .count(1)
                .extra(0);
    }

    @Override
    protected Sound getShootingSound() {
        return Sound.sound(Sounds.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, Sound.Source.PLAYER, 1, 1);
    }

    @Override
    protected Sound getReloadSound() {
        return Sound.sound(Sounds.GUN_DESERT_EAGLE_RELOAD, Sound.Source.PLAYER, 1, 1);
    }
}
