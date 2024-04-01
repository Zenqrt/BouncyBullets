package dev.zenqrt.bouncybullets.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;


public final class MiniMessageUtilsTests {

    @Test
    @DisplayName("Should wrap text ignoring tags in character count")
    void shouldWrapTextIgnoringTagsInCharacterCount() {
        String text = "<gray>Upon killing a player, receive a <aqua>Speed II <gray>effect for <green>5 <gray>seconds.";

        List<Component> expected = List.of(
                Component.text("Upon killing a player, receive a", NamedTextColor.GRAY),
                Component.text("Speed II ", NamedTextColor.AQUA)
                        .append(Component.text("effect for ", NamedTextColor.GRAY)
                        .append(Component.text("5 ", NamedTextColor.GREEN)
                        .append(Component.text("seconds.", NamedTextColor.GRAY))))
        );
        List<Component> actual = MiniMessageUtils.wordWrapLore(List.of(text), 30);

        Assertions.assertEquals(expected, actual);
    }
}
