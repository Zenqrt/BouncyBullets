package dev.zenqrt.bouncybullets.loadout;

import dev.zenqrt.bouncybullets.item.items.abilities.ActiveAbilityItem;
import dev.zenqrt.bouncybullets.item.items.guns.GunItem;
import dev.zenqrt.bouncybullets.loadout.kit.PlayerClass;
import dev.zenqrt.bouncybullets.utils.ItemUtils;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.Nullable;

public record Loadout(PlayerClass playerClass) {

    public void giveItems(PlayerInventory inventory) {
        for (GunItem gun : this.playerClass.getGuns()) {
            ItemStack itemStack = gun.buildItemStack();

            inventory.addItem(itemStack);
        }

        for (ActiveAbilityItem ability : this.playerClass.getActiveAbilities()) {
            ItemStack itemStack = ability.buildItemStack();

            inventory.addItem(itemStack);
        }

        ItemStack helmet = setupArmorPiece(this.playerClass.getArmorEquipment().get(EquipmentSlot.HEAD));
        ItemStack chestplate = setupArmorPiece(this.playerClass.getArmorEquipment().get(EquipmentSlot.CHEST));
        ItemStack leggings = setupArmorPiece(this.playerClass.getArmorEquipment().get(EquipmentSlot.LEGS));
        ItemStack boots = setupArmorPiece(this.playerClass.getArmorEquipment().get(EquipmentSlot.FEET));

        inventory.setHelmet(helmet);
        inventory.setChestplate(chestplate);
        inventory.setLeggings(leggings);
        inventory.setBoots(boots);
    }

    private ItemStack setupArmorPiece(@Nullable ItemStack itemStack) {
        if (itemStack == null)
            return null;

        ItemStack clone = ItemUtils.removeArmor(itemStack.clone());

        clone.editMeta(meta -> {
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
        });

        return clone;
    }

}
