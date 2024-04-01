package dev.zenqrt.bouncybullets.utils;

import org.bukkit.inventory.ItemStack;

public final class ItemUtils {

    public static ItemStack clone(ItemStack itemStack) {
        return new ItemStack(itemStack);
    }

}
