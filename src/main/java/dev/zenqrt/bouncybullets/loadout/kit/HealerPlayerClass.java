package dev.zenqrt.bouncybullets.loadout.kit;

import dev.zenqrt.bouncybullets.BouncyBulletsPlugin;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import dev.zenqrt.bouncybullets.item.GameItems;
import dev.zenqrt.bouncybullets.item.items.abilities.FullHealAbilityItem;
import dev.zenqrt.bouncybullets.item.items.guns.GunItem;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

final class HealerPlayerClass implements PlayerClass {

    private static final GunItem PRIMARY_GUN = GameItems.PISTOL;
    private static final FullHealAbilityItem ACTIVE_ABILITY = GameItems.FULL_HEAL;

    private final List<BukkitTask> healTasks = new ArrayList<>();

    @Override
    public void onStartUse(BouncyBulletGamePlayer player) {
        Player playerEntity = player.getPlayer();
        Objects.requireNonNull(playerEntity.getAttribute(Attribute.GENERIC_MAX_ABSORPTION)).setBaseValue(5);

        healTasks.add(new BukkitRunnable() {
            @Override
            public void run() {
                if (playerEntity.getGameMode() != GameMode.ADVENTURE) {
                    return;
                }

                double maxHealth = Objects.requireNonNull(playerEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue();

                if (playerEntity.getHealth() < maxHealth) {
                    playerEntity.setHealth(Math.min(maxHealth, playerEntity.getHealth() + 1));
                }
            }
        }.runTaskTimer(BouncyBulletsPlugin.getInstance(), 0, 40));
    }

    @Override
    public void onStopUse(BouncyBulletGamePlayer player) {
        this.healTasks.forEach(BukkitTask::cancel);
        this.healTasks.clear();
    }

    @Override
    public String getName() {
        return "Healer";
    }

    @Override
    public TreeMap<Integer, ItemStack> getItems() {
        return new TreeMap<>() {
            {
                put(0, PRIMARY_GUN.buildItemStack());
                put(1, ACTIVE_ABILITY.buildItemStack());
            }
        };
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
