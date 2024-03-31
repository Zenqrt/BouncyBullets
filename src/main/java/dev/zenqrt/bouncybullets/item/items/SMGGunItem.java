package dev.zenqrt.bouncybullets.item.items;

import com.destroystokyo.paper.ParticleBuilder;
import dev.zenqrt.bouncybullets.game.games.Gun;
import dev.zenqrt.bouncybullets.utils.AdventureUtils;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Particle;

public final class SMGGunItem extends GunItem {

    public SMGGunItem(Gun gun) {
        super("smg", Material.IRON_HORSE_ARMOR, AdventureUtils.noItalic(Component.text("SMG", NamedTextColor.YELLOW)), gun);
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
