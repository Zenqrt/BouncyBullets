package dev.zenqrt.bouncybullets.loadout.kit;

import dev.zenqrt.bouncybullets.item.GameItems;
import dev.zenqrt.bouncybullets.item.items.abilities.ActiveAbilityItem;
import dev.zenqrt.bouncybullets.item.items.guns.GunItem;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public final class SidewinderPlayerClass implements PlayerClass {

    @Override
    public String getName() {
        return "Sidewinder";
    }

    @Override
    public List<GunItem> getGuns() {
        return List.of(
                GameItems.SPECIAL_BURST_RIFLE
        );
    }

    @Override
    public List<ActiveAbilityItem> getActiveAbilities() {
        return List.of();
    }

    @Override
    public Map<EquipmentSlot, ItemStack> getArmorEquipment() {
        return Map.of();
    }
}
