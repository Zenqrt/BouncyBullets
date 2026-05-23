package dev.zenqrt.bouncybullets.utils;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class GuiUtils {

    public static Pane createBackgroundPane(int rows) {
        OutlinePane pane = new OutlinePane(9, rows, Pane.Priority.LOWEST);
        pane.setRepeat(true);

        ItemStack background = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        background.editMeta(meta -> meta.displayName(Component.empty()));

        pane.addItem(new GuiItem(background));

        return pane;
    }

}
