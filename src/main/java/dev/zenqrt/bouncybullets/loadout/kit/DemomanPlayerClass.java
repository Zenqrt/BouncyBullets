package dev.zenqrt.bouncybullets.loadout.kit;

import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import dev.zenqrt.bouncybullets.item.GameItems;
import dev.zenqrt.bouncybullets.item.items.abilities.RailgunAbilityItem;
import dev.zenqrt.bouncybullets.item.items.guns.GunItem;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

final class DemomanPlayerClass implements PlayerClass {

    private static final AttributeModifier SPEED_MODIFIER = new AttributeModifier("demoman_speed_modifier", -0.02, AttributeModifier.Operation.ADD_NUMBER);
    private static final GunItem PRIMARY_GUN = GameItems.GRENADE_LAUNCHER;
    private static final GunItem SECONDARY_GUN = GameItems.DESERT_EAGLE;
    private static final RailgunAbilityItem ACTIVE_ABILITY = GameItems.RAILGUN;

    @Override
    public void onStartUse(BouncyBulletGamePlayer gamePlayer) {
        Objects.requireNonNull(gamePlayer.getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).addModifier(SPEED_MODIFIER);
    }

    @Override
    public void onStopUse(BouncyBulletGamePlayer gamePlayer) {
        Objects.requireNonNull(gamePlayer.getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).removeModifier(SPEED_MODIFIER);
    }

    @Override
    public String getName() {
        return "Demoman";
    }

    @Override
    public TreeMap<Integer, ItemStack> getItems() {
        return new TreeMap<>() {{
            put(0, PRIMARY_GUN.buildItemStack());
            put(1, SECONDARY_GUN.buildItemStack());
            put(2, ACTIVE_ABILITY.buildItemStack());
        }};
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
