package dev.zenqrt.bouncybullets.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class AdventureUtils {

    public static Component withoutItalics(String text, TextColor textColor) {
        return Component.text(text, textColor).decoration(TextDecoration.ITALIC, false);
    }

    public static Component withoutItalics(String text) {
        return MiniMessage.miniMessage().deserialize(text).decoration(TextDecoration.ITALIC, false);
    }
}
