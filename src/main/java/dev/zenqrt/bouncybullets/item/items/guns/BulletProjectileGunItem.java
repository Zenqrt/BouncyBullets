package dev.zenqrt.bouncybullets.item.items.guns;

import com.destroystokyo.paper.ParticleBuilder;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.loadout.gun.BulletProperties;
import dev.zenqrt.bouncybullets.loadout.gun.GunProperties;
import dev.zenqrt.bouncybullets.tasks.ShootBulletTask;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public abstract class BulletProjectileGunItem extends GunItem {

    public BulletProjectileGunItem(String key, Material material, String displayName, GunProperties gunProperties, BulletProperties bulletProperties) {
        super(key, material, displayName, gunProperties, bulletProperties);
    }

    protected abstract ParticleBuilder getBulletParticleBuilder();

    @Override
    protected void shootProjectile(BouncyBulletGame game, Player player, BulletProperties bulletProperties) {
        new ShootBulletTask(
                player,
                player.getEyeLocation(),
                player.getEyeLocation().getDirection().normalize(),
                bulletProperties,
                GunItem.isAiming(player) ? this.gunProperties.recoilRangeFocused() : this.gunProperties.recoilRange(),
                getBulletParticleBuilder()
        ).runTaskTimer(game.getPlugin(), 0, 1);
    }
}
