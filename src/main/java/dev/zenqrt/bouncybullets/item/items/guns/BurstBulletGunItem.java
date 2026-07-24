package dev.zenqrt.bouncybullets.item.items.guns;

import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import dev.zenqrt.bouncybullets.loadout.gun.BulletProperties;
import dev.zenqrt.bouncybullets.loadout.gun.GunProperties;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class BurstBulletGunItem extends BulletGunItem {

    private final int burstRounds;

    public BurstBulletGunItem(
            String key,
            String displayName,
            GunProperties gunProperties,
            BulletProperties bulletProperties,
            TipOffset tipOffset,
            TipOffset tipOffsetAiming,
            int burstRounds
    ) {
        super(key, displayName, gunProperties, bulletProperties, tipOffset, tipOffsetAiming);

        this.burstRounds = burstRounds;
    }

    @Override
    protected void useGun(BouncyBulletGame game, BouncyBulletGamePlayer gamePlayer, Player player, ItemStack itemStack) {
        long currentGameTime = player.getServer().getCurrentTick();
        int ammo = getAmmo(itemStack);

        if (ammo <= 0)
            return;

        if (super.lastShootTicks.containsKey(player.getUniqueId())) {
            long lastShootTick = super.lastShootTicks.get(player.getUniqueId());
            long tickInterval = currentGameTime - lastShootTick;

            if (tickInterval < super.gunProperties.shootDelayTicks()) {
                return;
            }
        }

        int rounds = Math.min(ammo, this.burstRounds);

        for (int i = 0; i < rounds; i++) {
            Bukkit.getScheduler().runTaskLater(
                    game.getPlugin(),
                    () -> shootGun(game, gamePlayer, gamePlayer.getOldHud(), itemStack),
                    i * 2L
            );
        }

        super.lastShootTicks.put(player.getUniqueId(), currentGameTime + rounds);
    }
}
