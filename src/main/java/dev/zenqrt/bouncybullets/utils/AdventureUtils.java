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
        StringBuilder lastTag = new StringBuilder();
        int currentLength = 0;
        boolean inTag = false;

        for (char ch : text.toCharArray()) {
            if (ch == '<') {
                inTag = true;

                if (currentLength + word.length() > wrapLength) {
                    output.append('\n').append(lastTag);
                    currentLength = 0;
                }

                output.append(word);
                currentLength += word.length();
                word.setLength(0);
                lastTag.setLength(0);
                lastTag.append(ch);

                output.append(ch);
            } else if (ch == '>') {
                inTag = false;
                output.append(ch);
                lastTag.append(ch);
            } else if (!inTag) {
                word.append(ch);
                if (ch == ' ' && currentLength + word.length() > wrapLength) {
                    output.append(word).append('\n').append(lastTag);
                    word.setLength(0);
                    currentLength = 0;
                }
            } else {
                output.append(ch);
                lastTag.append(ch);
            }
        }

        if (!word.isEmpty()) {
            if (currentLength + word.length() > wrapLength) {
                output.append('\n').append(lastTag);
            }
            output.append(word);
        }

        return output.toString();
    }

}
