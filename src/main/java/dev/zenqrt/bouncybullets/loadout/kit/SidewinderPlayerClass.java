package dev.zenqrt.bouncybullets.loadout.kit;

import dev.zenqrt.bouncybullets.item.GameItems;
import dev.zenqrt.bouncybullets.item.items.guns.GunItem;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public final class SidewinderPlayerClass implements PlayerClass {

    private static final GunItem PRIMARY_GUN = GameItems.SPECIAL_BURST_RIFLE;

    @Override
    public String getName() {
        return "Sidewinder";
    }

    @Override
    public Map<Integer, ItemStack> getItems() {
        return Map.of(
                0, PRIMARY_GUN.buildItemStack()
        );
    }

    @Override
    public Map<EquipmentSlot, ItemStack> getArmorEquipment() {
        return Map.of();
    }
}
