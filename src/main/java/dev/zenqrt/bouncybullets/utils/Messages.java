package dev.zenqrt.bouncybullets.utils;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public final class Messages {

    private Messages() {}

    public static void sendCommandSuccess(Audience audience, String message) {
        audience.sendMessage(Component.text(message, NamedTextColor.GREEN));
    }

    public static void sendCommandInfo(Audience audience, String message) {
        audience.sendMessage(Component.text(message, NamedTextColor.GRAY).decorate(TextDecoration.ITALIC));
    }

}
