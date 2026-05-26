package dev.zenqrt.bouncybullets.loadout.kit;

import dev.zenqrt.bouncybullets.BouncyBulletsPlugin;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import dev.zenqrt.bouncybullets.item.GameItems;
import dev.zenqrt.bouncybullets.item.items.abilities.ActiveAbilityItem;
import dev.zenqrt.bouncybullets.item.items.guns.GunItem;
import dev.zenqrt.bouncybullets.utils.PlayerUtils;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

final class DemomanPlayerClass implements PlayerClass {

    private static final AttributeModifier SPEED_MODIFIER = new AttributeModifier(
            BouncyBulletsPlugin.createKey("demoman_speed_modifier"),
            -0.02,
            AttributeModifier.Operation.ADD_NUMBER
    );

    @Override
    public void onStartUse(BouncyBulletGamePlayer gamePlayer) {
        PlayerUtils.requireNonNullAttribute(gamePlayer.getPlayer(), Attribute.MOVEMENT_SPEED)
                        .addTransientModifier(SPEED_MODIFIER);
    }

    @Override
    public void onStopUse(BouncyBulletGamePlayer gamePlayer) {
        PlayerUtils.requireNonNullAttribute(gamePlayer.getPlayer(), Attribute.MOVEMENT_SPEED)
                .removeModifier(SPEED_MODIFIER);
    }

    @Override
    public String getName() {
        return "Demoman";
    }

    @Override
    public List<GunItem> getGuns() {
        return List.of(
                GameItems.GRENADE_LAUNCHER,
                GameItems.DESERT_EAGLE
        );
    }

    @Override
    public List<ActiveAbilityItem> getActiveAbilities() {
        return List.of(
                GameItems.DEMOMAN_ACTIVE_ABILITY
        );
    }

    @Override
    public Map<EquipmentSlot, ItemStack> getArmorEquipment() {
        return Map.of(
                EquipmentSlot.HEAD, new ItemStack(Material.BLAST_FURNACE),
                EquipmentSlot.CHEST, new ItemStack(Material.NETHERITE_CHESTPLATE),
                EquipmentSlot.LEGS, new ItemStack(Material.CHAINMAIL_LEGGINGS),
                EquipmentSlot.FEET, new ItemStack(Material.IRON_BOOTS)
        );
    }
}
