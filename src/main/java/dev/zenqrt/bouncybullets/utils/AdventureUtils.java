package dev.zenqrt.bouncybullets.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public final class AdventureUtils {

    public static Component withoutItalics(String text, TextColor textColor) {
        return Component.text(text, textColor).decoration(TextDecoration.ITALIC, false);
    }

    public static Component withShadow(String text, TextColor color) {
        return Component.text(text, color)
                .shadowColor(ShadowColor.shadowColor(0, 0, 0, 128));
    }
}
