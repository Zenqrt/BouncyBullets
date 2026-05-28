package dev.zenqrt.bouncybullets.utils;

import dev.zenqrt.bouncybullets.BouncyBulletsPlugin;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
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
                        Attribute.ARMOR,
                        new AttributeModifier(BouncyBulletsPlugin.createKey("remove_armor"), 0, AttributeModifier.Operation.ADD_NUMBER)
                )
        );

        return itemStack;
    }

    @SuppressWarnings("UnstableApiUsage")
    public static ItemStack createWithItemName(Material material, Component itemName) {
        ItemStack itemStack = new ItemStack(material);
        itemStack.setData(DataComponentTypes.ITEM_NAME, itemName);

        return itemStack;
    }

}
