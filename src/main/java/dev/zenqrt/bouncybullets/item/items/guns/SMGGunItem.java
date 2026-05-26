package dev.zenqrt.bouncybullets.item.items.guns;

import com.destroystokyo.paper.ParticleBuilder;
import dev.zenqrt.bouncybullets.loadout.gun.BulletProperties;
import dev.zenqrt.bouncybullets.loadout.gun.GunProperties;
import dev.zenqrt.bouncybullets.utils.Sounds;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.Particle;

public final class SMGGunItem extends BulletGunItem {

    public SMGGunItem(GunProperties gunProperties, BulletProperties bulletProperties) {
        super("smg", Material.NETHERITE_HORSE_ARMOR, "SMG", gunProperties, bulletProperties);
    }

    @Override
    protected Sound getShootingSound() {
        return Sound.sound(Sounds.ENTITY_ARROW_SHOOT, Sound.Source.PLAYER, 1, 2);
    }

    @Override
    protected ParticleBuilder getBulletParticleBuilder() {
        return Particle.ASH.builder();
    }
}
