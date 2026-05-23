package dev.zenqrt.bouncybullets.utils;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class GuiUtils {

    @SuppressWarnings("UnstableApiUsage")
    public static Pane createBackgroundPane(int rows) {
        OutlinePane pane = new OutlinePane(9, rows, Pane.Priority.LOWEST);
        pane.setRepeat(true);

        ItemStack background = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        background.setData(
                DataComponentTypes.TOOLTIP_DISPLAY,
                TooltipDisplay.tooltipDisplay()
                        .hideTooltip(true)
                        .build()
        );

        pane.addItem(new GuiItem(background));

        return pane;
    }

}
