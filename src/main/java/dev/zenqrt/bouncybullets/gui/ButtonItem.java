package dev.zenqrt.bouncybullets.gui;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import net.kyori.adventure.sound.Sound;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ButtonItem extends GuiItem {

    public ButtonItem(@NotNull ItemStack item, @Nullable Sound rightClickSound, @Nullable Sound leftClickSound, @NotNull Consumer<InventoryClickEvent> action) {
        super(item, action.andThen(event -> {
            if (event.isLeftClick() && leftClickSound != null) {
                event.getWhoClicked().playSound(leftClickSound, Sound.Emitter.self());
            } else if (event.isRightClick() && rightClickSound != null) {
                event.getWhoClicked().playSound(rightClickSound, Sound.Emitter.self());
            }
        }));
    }

    public ButtonItem(@NotNull ItemStack item, @NotNull Consumer<InventoryClickEvent> action) {
        this(item, UISounds.BUTTON_CLICK, UISounds.BUTTON_CLICK, action);
    }

    public ButtonItem(@NotNull ItemStack item, @NotNull Sound leftClickSound, @NotNull Consumer<InventoryClickEvent> action) {
        this(item, null, leftClickSound, action);
    }
}
