package dev.zenqrt.bouncybullets.item.items.guns;

import com.destroystokyo.paper.ParticleBuilder;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.loadout.gun.BulletProperties;
import dev.zenqrt.bouncybullets.loadout.gun.GunProperties;
import dev.zenqrt.bouncybullets.utils.Sounds;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public final class TwinPistolGunItem extends BulletGunItem {

    public TwinPistolGunItem(GunProperties gunProperties, BulletProperties bulletProperties) {
        super("twin_pistol", "Twin Pistol", gunProperties, bulletProperties);
    }

    @Override
    protected ParticleBuilder getBulletParticleBuilder() {
        return Particle.CRIT.builder()
                .count(1)
                .extra(0);
    }

    @Override
    protected Sound getShootingSound() {
        return Sound.sound(Sounds.ENTITY_IRON_GOLEM_HURT, Sound.Source.PLAYER, 1, 2);
    }

    @Override
    public void onHeld(BouncyBulletGame game, Player player, ItemStack itemStack, ItemStack previousItemStack) {
        ItemStack twinItem = new ItemStack(super.material);
        twinItem.editMeta(itemMeta -> {
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemMeta.displayName(itemStack.getItemMeta().displayName());
        });

        player.getInventory().setItemInOffHand(twinItem);

        super.onHeld(game, player, itemStack, previousItemStack);
    }

    @Override
    public void onUnheld(BouncyBulletGame game, Player player, ItemStack itemStack, ItemStack newItemStack) {
        player.getInventory().setItemInOffHand(null);

        super.onUnheld(game, player, itemStack, newItemStack);
    }
}
