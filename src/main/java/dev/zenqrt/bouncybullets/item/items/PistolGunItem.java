package dev.zenqrt.bouncybullets.item.items;

import com.destroystokyo.paper.ParticleBuilder;
import dev.zenqrt.bouncybullets.game.games.BulletProperties;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Particle;

public final class PistolGunItem extends GunItem {

    public PistolGunItem(BulletProperties properties) {
        super("pistol", Material.GOLDEN_HOE, Component.text("BB (Boing Boing) Pistol", NamedTextColor.YELLOW), properties);
    }

    @Override
    protected Sound getShootingSound() {
        return Sound.sound(org.bukkit.Sound.ENTITY_IRON_GOLEM_DAMAGE.key(), Sound.Source.PLAYER, 1, 2);
    }

    @Override
    protected ParticleBuilder getBulletParticleBuilder() {
        return Particle.CRIT.builder()
                .count(1)
                .extra(0);
    }
}
