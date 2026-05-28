package dev.zenqrt.bouncybullets.utils;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.util.Gui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import dev.zenqrt.bouncybullets.gui.ButtonItem;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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

    public static Pane createBackButtonPane(Gui lastGui) {
        StaticPane pane = new StaticPane(9, 1);

        ItemStack backItemStack = ItemUtils.createWithItemName(
                Material.ARROW,
                Component.text("Back", NamedTextColor.GRAY)
        );

        GuiItem back = new ButtonItem(
                backItemStack,
                event -> {
                    event.setCancelled(true);
                    lastGui.show(event.getWhoClicked());
                }
        );

        pane.addItem(back, 0, 0);

        return pane;
    }

}
