package dev.zenqrt.bouncybullets.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

public final class AdventureUtils {

    public static Component withoutItalics(String text, TextColor textColor) {
        return Component.text(text, textColor).decoration(TextDecoration.ITALIC, false);
    }

    public static String wrapTextIgnoringTags(String text, int wrapLength) {
        if (text.isEmpty() || text.isBlank())
            return text;

        StringBuilder output = new StringBuilder();
        StringBuilder word = new StringBuilder();
        int currentLength = 0;
        boolean inTag = false;

        for (char ch : text.toCharArray()) {
            if (ch == '<') {
                inTag = true;

                if (currentLength + word.length() > wrapLength) {
                    System.out.println("IN tag append at " + currentLength);
                    output.append('\n');
                    currentLength = 0;
                }

                output.append(word);
                currentLength += word.length();
                word.setLength(0);

                output.append(ch);
            } else if (ch == '>') {
                inTag = false;
                output.append(ch);
            } else if (!inTag) {
                word.append(ch);
                if (ch == ' ' && currentLength + word.length() > wrapLength) {
                    System.out.println("Regular append at " + currentLength);
                    output.append(word).append('\n');
                    word.setLength(0);
                    currentLength = 0;
                }
            } else {
                output.append(ch);
            }
        }

        if (!word.isEmpty()) {
            if (currentLength + word.length() > wrapLength) {
                System.out.println("idk what this is at " + currentLength);
                output.append('\n');
            }
            output.append(word);
        }

        return output.toString();
    }

}
