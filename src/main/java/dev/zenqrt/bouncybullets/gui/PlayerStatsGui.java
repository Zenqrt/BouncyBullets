package dev.zenqrt.bouncybullets.gui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import dev.zenqrt.bouncybullets.stats.PlayerStats;
import dev.zenqrt.bouncybullets.utils.DecimalFormats;
import dev.zenqrt.bouncybullets.utils.GuiUtils;
import dev.zenqrt.bouncybullets.utils.ItemUtils;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class PlayerStatsGui extends ChestGui {

    public PlayerStatsGui(String username, PlayerStats stats) {
        super(3, username + "'s Stats");

        this.setOnGlobalClick(event -> event.setCancelled(true));

        this.addPane(Slot.fromXY(0, 0), GuiUtils.createBackgroundPane(super.getRows()));
        this.addPane(Slot.fromXY(1, 1), createStatsPane(username, stats));
    }

    @SuppressWarnings("UnstableApiUsage")
    private Pane createStatsPane(String username, PlayerStats stats) {
        StaticPane pane = new StaticPane(7, 3);

        ItemStack statsItem = ItemUtils.createWithItemName(
                Material.DIAMOND_HOE,
                Component.text("Bouncy Bullets Stats", NamedTextColor.YELLOW)
                        .decorate(TextDecoration.BOLD)
        );

        statsItem.setData(
                DataComponentTypes.LORE,
                ItemLore.lore()
                        .addLine(createStatText("Games played", stats.getGamesPlayed(), NamedTextColor.AQUA))
                        .addLine(Component.empty())
                        .addLine(createStatText("Total kills", stats.getTotalKills(), NamedTextColor.GREEN))
                        .addLine(createStatText("Total deaths", stats.getTotalDeaths(), NamedTextColor.GREEN))
                        .addLine(createStatText("Total wins", stats.getTotalWins(), NamedTextColor.GREEN))
                        .addLine(createStatText("Total losses", stats.getTotalLosses(), NamedTextColor.GREEN))
                        .addLine(Component.empty())
                        .addLine(createStatText("KDR", DecimalFormats.SHORT_DECIMAL_FORMAT_2.format(stats.getTotalKillDeathRatio()), NamedTextColor.RED))
                        .addLine(createStatText("WLR", DecimalFormats.SHORT_DECIMAL_FORMAT_2.format(stats.getTotalWinLossRatio()), NamedTextColor.AQUA))
        );
        statsItem.setData(
                DataComponentTypes.TOOLTIP_DISPLAY,
                TooltipDisplay.tooltipDisplay()
                        .addHiddenComponents(DataComponentTypes.ATTRIBUTE_MODIFIERS)
        );

        ItemStack classStatsItem = ItemUtils.createWithItemName(
                Material.NETHER_STAR,
                Component.text("Class Stats", NamedTextColor.YELLOW)
                        .decorate(TextDecoration.BOLD)
        );

        pane.addItem(new GuiItem(statsItem), 1, 0);
        pane.addItem(
                new GuiItem(
                        classStatsItem,
                        event -> new ClassStatsGui(username, stats, this).show(event.getWhoClicked())
                ),
                5, 0
        );

        return pane;
    }

    private static Component createStatText(String stat, Object value, TextColor valueColor) {
        return Component.text(stat + ": ", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text(value.toString(), valueColor));
    }
}
