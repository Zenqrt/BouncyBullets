package dev.zenqrt.bouncybullets.loadout.kit;

import dev.zenqrt.bouncybullets.BouncyBulletsPlugin;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import dev.zenqrt.bouncybullets.item.GameItems;
import dev.zenqrt.bouncybullets.item.items.abilities.ActiveAbilityItem;
import dev.zenqrt.bouncybullets.item.items.guns.GunItem;
import dev.zenqrt.bouncybullets.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class HealerPlayerClass implements PlayerClass {

    private final List<BukkitTask> healTasks = new ArrayList<>();

    @Override
    public void onStartUse(BouncyBulletGamePlayer gamePlayer) {
        Player player = gamePlayer.getPlayer();

        PlayerUtils.requireNonNullAttribute(player, Attribute.MAX_ABSORPTION)
                        .setBaseValue(5);

        this.healTasks.add(
                Bukkit.getScheduler().runTaskTimer(
                        BouncyBulletsPlugin.getInstance(),
                        () -> {
                            if (gamePlayer.isDead())
                                return;

                            double maxHealth = PlayerUtils.requireNonNullAttribute(player, Attribute.MAX_HEALTH).getValue();

                            if (player.getHealth() < maxHealth)
                                player.heal(1);
                        },
                        0, 40
                )
        );
    }

    @Override
    public void onStopUse(BouncyBulletGamePlayer gamePlayer) {
        this.healTasks.forEach(BukkitTask::cancel);
        this.healTasks.clear();
    }

    @Override
    public String getName() {
        return "Healer";
    }

    @Override
    public List<GunItem> getGuns() {
        return List.of(
                GameItems.PISTOL
        );
    }

    @Override
    public List<ActiveAbilityItem> getActiveAbilities() {
        return List.of(
                GameItems.HEALER_ACTIVE_ABILITY
        );
    }

    @Override
    public Map<EquipmentSlot, ItemStack> getArmorEquipment() {
        return Map.of(
                EquipmentSlot.HEAD, new ItemStack(Material.FLOWERING_AZALEA),
                EquipmentSlot.CHEST, new ItemStack(Material.GOLDEN_CHESTPLATE),
                EquipmentSlot.LEGS, new ItemStack(Material.CHAINMAIL_LEGGINGS),
                EquipmentSlot.FEET, new ItemStack(Material.IRON_BOOTS)
        );
    }
}
