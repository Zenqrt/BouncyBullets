package dev.zenqrt.bouncybullets.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

public final class AdventureUtils {

    public static Component noItalic(Component component) {
        return component.decoration(TextDecoration.ITALIC, false);
    }

}
