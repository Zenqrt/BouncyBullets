package dev.zenqrt.bouncybullets.utils;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public final class ItemUtils {

    public static ItemStack clone(ItemStack itemStack) {
        return new ItemStack(itemStack);
    }

    public static ItemStack createLeatherArmor(Material material, Color color) {
        ItemStack itemStack = new ItemStack(material);
        itemStack.editMeta(LeatherArmorMeta.class, meta -> meta.setColor(color));

        return itemStack;
    }

    public static ItemStack removeArmor(ItemStack itemStack) {
        itemStack.editMeta(
                meta -> meta.addAttributeModifier(
                        Attribute.GENERIC_ARMOR,
                        new AttributeModifier("remove_armor", 0, AttributeModifier.Operation.ADD_NUMBER)
                )
        );

        return itemStack;
    }

}
