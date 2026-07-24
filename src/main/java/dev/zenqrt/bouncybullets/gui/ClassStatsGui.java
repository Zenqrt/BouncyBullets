package dev.zenqrt.bouncybullets.gui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import dev.zenqrt.bouncybullets.loadout.kit.PlayerClassType;
import dev.zenqrt.bouncybullets.stats.PlayerClassStats;
import dev.zenqrt.bouncybullets.stats.PlayerStats;
import dev.zenqrt.bouncybullets.utils.AdventureUtils;
import dev.zenqrt.bouncybullets.utils.GuiUtils;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public final class ClassStatsGui extends ChestGui {

    @SuppressWarnings("UnstableApiUsage")
    public ClassStatsGui(String username, PlayerStats stats, ChestGui previousGui) {
        super(4, username + "'s Class Stats");

        this.setOnGlobalClick(event -> event.setCancelled(true));

        OutlinePane classStatsPane = new OutlinePane(7, 1);

        for (PlayerClassType classType : PlayerClassType.VALUES) {
            PlayerClassStats classStats = Objects.requireNonNullElse(
                    stats.getClassStats(classType),
                    new PlayerClassStats()
            );

            ItemStack itemStack = classType.getSelectionUIIcon().clone();
            itemStack.setData(
                    DataComponentTypes.CUSTOM_NAME,
                    AdventureUtils.withoutItalics(classType.getPlayerClass().getName(), NamedTextColor.YELLOW)
                            .decorate(TextDecoration.BOLD)

            );
            itemStack.setData(
                    DataComponentTypes.LORE,
                    ItemLore.lore()
                            .addLine(createStatText("Kills", classStats.getKills(), NamedTextColor.GREEN))
                            .addLine(createStatText("Deaths", classStats.getDeaths(), NamedTextColor.GREEN))
                            .addLine(Component.empty())
                            .addLine(createStatText("Wins", classStats.getWins(), NamedTextColor.GREEN))
                            .addLine(createStatText("Losses", classStats.getLosses(), NamedTextColor.GREEN))
            );
            itemStack.setData(
                    DataComponentTypes.TOOLTIP_DISPLAY,
                    TooltipDisplay.tooltipDisplay()
                            .addHiddenComponents(
                                    DataComponentTypes.POTION_CONTENTS,
                                    DataComponentTypes.ATTRIBUTE_MODIFIERS
                            )
            );

            classStatsPane.addItem(new GuiItem(itemStack));
        }

        this.addPane(Slot.fromXY(1, 1), classStatsPane);
        this.addPane(Slot.fromXY(0, 0), GuiUtils.createBackgroundPane(super.getRows()));
        this.addPane(Slot.fromXY(0, 3), GuiUtils.createBackButtonPane(previousGui));
    }

    private static Component createStatText(String stat, Object value, TextColor valueColor) {
        return Component.text(stat + ": ", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text(value.toString(), valueColor));
    }
}
