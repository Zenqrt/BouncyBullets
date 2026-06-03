package dev.zenqrt.bouncybullets.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class MiniMessageUtils {

    private static final Pattern NEW_LINE_PATTERN = Pattern.compile("\n");

    public static List<Component> wordWrapLore(List<String> lore, int wordWrapLength) {
        MiniMessage miniMessage = MiniMessage.builder().preProcessor(string -> {
            if (string.length() > wordWrapLength) {
                return wrapTextIgnoringTags(string, wordWrapLength);
            }

            return string;
        }).build();


        List<Component> components = new ArrayList<>();

        lore.forEach(string -> {
            Component component = miniMessage.deserialize(string);
            components.addAll(ComponentSplitting.split(component, NEW_LINE_PATTERN));
        });

        return components;
    }

    // TODO: Needs a rewrite to accomodate for </tag> tags
    private static String wrapTextIgnoringTags(String text, int wrapLength) {
        if (text.isBlank())
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
                if (ch == ' ' && currentLength + word.length() > wrapLength) {
                    output.append(word).append('\n').append(lastTag);
                    word.setLength(0);
                    currentLength = 0;
                } else {
                    word.append(ch);

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
