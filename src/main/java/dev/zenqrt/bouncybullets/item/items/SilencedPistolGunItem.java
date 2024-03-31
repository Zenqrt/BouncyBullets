package dev.zenqrt.bouncybullets.item.items;

import com.destroystokyo.paper.ParticleBuilder;
import dev.zenqrt.bouncybullets.game.games.Gun;
import dev.zenqrt.bouncybullets.utils.AdventureUtils;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Particle;

public final class SilencedPistolGunItem extends GunItem {

    public SilencedPistolGunItem(Gun gun) {
        super("silenced_pistol", Material.IRON_HOE, AdventureUtils.noItalic(Component.text("Silenced Pistol", NamedTextColor.YELLOW)), gun);
    }

    @Override
    protected Sound getShootingSound() {
        return Sound.sound(org.bukkit.Sound.ENTITY_FIREWORK_ROCKET_BLAST, Sound.Source.PLAYER, 1, 2);
    }

    @Override
    protected ParticleBuilder getBulletParticleBuilder() {
        return Particle.CRIT.builder()
                .extra(0.01);
    }
}
