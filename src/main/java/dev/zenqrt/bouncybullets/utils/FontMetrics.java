package dev.zenqrt.bouncybullets.utils;

import it.unimi.dsi.fastutil.chars.Char2IntMap;
import it.unimi.dsi.fastutil.chars.Char2IntOpenHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ObjectComponent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.SpriteObjectContents;

import java.util.ArrayList;
import java.util.List;

public final class FontMetrics {

    private static final Char2IntMap WIDTHS = new Char2IntOpenHashMap();

    static {
        WIDTHS.put('\u0000', 0);
        WIDTHS.put('!', 1);
        WIDTHS.put('"', 3);
        WIDTHS.put('#', 5);
        WIDTHS.put('$', 5);
        WIDTHS.put('%', 5);
        WIDTHS.put('&', 5);
        WIDTHS.put('\'', 1);
        WIDTHS.put('(', 3);
        WIDTHS.put(')', 3);
        WIDTHS.put('*', 3);
        WIDTHS.put('+', 5);
        WIDTHS.put(',', 1);
        WIDTHS.put('-', 5);
        WIDTHS.put('.', 1);
        WIDTHS.put('/', 5);
        WIDTHS.put('0', 5);
        WIDTHS.put('1', 5);
        WIDTHS.put('2', 5);
        WIDTHS.put('3', 5);
        WIDTHS.put('4', 5);
        WIDTHS.put('5', 5);
        WIDTHS.put('6', 5);
        WIDTHS.put('7', 5);
        WIDTHS.put('8', 5);
        WIDTHS.put('9', 5);
        WIDTHS.put(':', 1);
        WIDTHS.put(';', 1);
        WIDTHS.put('<', 4);
        WIDTHS.put('=', 5);
        WIDTHS.put('>', 4);
        WIDTHS.put('?', 5);
        WIDTHS.put('@', 6);
        WIDTHS.put('A', 5);
        WIDTHS.put('B', 5);
        WIDTHS.put('C', 5);
        WIDTHS.put('D', 5);
        WIDTHS.put('E', 5);
        WIDTHS.put('F', 5);
        WIDTHS.put('G', 5);
        WIDTHS.put('H', 5);
        WIDTHS.put('I', 3);
        WIDTHS.put('J', 5);
        WIDTHS.put('K', 5);
        WIDTHS.put('L', 5);
        WIDTHS.put('M', 5);
        WIDTHS.put('N', 5);
        WIDTHS.put('O', 5);
        WIDTHS.put('P', 5);
        WIDTHS.put('Q', 5);
        WIDTHS.put('R', 5);
        WIDTHS.put('S', 5);
        WIDTHS.put('T', 5);
        WIDTHS.put('U', 5);
        WIDTHS.put('V', 5);
        WIDTHS.put('W', 5);
        WIDTHS.put('X', 5);
        WIDTHS.put('Y', 5);
        WIDTHS.put('Z', 5);
        WIDTHS.put('[', 3);
        WIDTHS.put('\\', 5);
        WIDTHS.put(']', 3);
        WIDTHS.put('^', 5);
        WIDTHS.put('_', 5);
        WIDTHS.put('`', 2);
        WIDTHS.put('a', 5);
        WIDTHS.put('b', 5);
        WIDTHS.put('c', 5);
        WIDTHS.put('d', 5);
        WIDTHS.put('e', 5);
        WIDTHS.put('f', 4);
        WIDTHS.put('g', 5);
        WIDTHS.put('h', 5);
        WIDTHS.put('i', 1);
        WIDTHS.put('j', 5);
        WIDTHS.put('k', 4);
        WIDTHS.put('l', 2);
        WIDTHS.put('m', 5);
        WIDTHS.put('n', 5);
        WIDTHS.put('o', 5);
        WIDTHS.put('p', 5);
        WIDTHS.put('q', 5);
        WIDTHS.put('r', 5);
        WIDTHS.put('s', 5);
        WIDTHS.put('t', 3);
        WIDTHS.put('u', 5);
        WIDTHS.put('v', 5);
        WIDTHS.put('w', 5);
        WIDTHS.put('x', 5);
        WIDTHS.put('y', 5);
        WIDTHS.put('z', 5);
        WIDTHS.put('{', 3);
        WIDTHS.put('|', 1);
        WIDTHS.put('}', 3);
        WIDTHS.put('~', 6);
        WIDTHS.put('£', 5);
        WIDTHS.put('ƒ', 5);
        WIDTHS.put('ª', 4);
        WIDTHS.put('º', 4);
        WIDTHS.put('¬', 5);
        WIDTHS.put('«', 6);
        WIDTHS.put('»', 6);
        WIDTHS.put('░', 7);
        WIDTHS.put('▒', 8);
        WIDTHS.put('▓', 8);
        WIDTHS.put('│', 5);
        WIDTHS.put('┤', 5);
        WIDTHS.put('╡', 5);
        WIDTHS.put('╢', 7);
        WIDTHS.put('╖', 7);
        WIDTHS.put('╕', 5);
        WIDTHS.put('╣', 7);
        WIDTHS.put('║', 7);
        WIDTHS.put('╗', 7);
        WIDTHS.put('╝', 7);
        WIDTHS.put('╜', 7);
        WIDTHS.put('╛', 5);
        WIDTHS.put('┐', 5);
        WIDTHS.put('└', 8);
        WIDTHS.put('┴', 8);
        WIDTHS.put('┬', 8);
        WIDTHS.put('├', 8);
        WIDTHS.put('─', 8);
        WIDTHS.put('┼', 8);
        WIDTHS.put('╞', 8);
        WIDTHS.put('╟', 8);
        WIDTHS.put('╚', 8);
        WIDTHS.put('╔', 8);
        WIDTHS.put('╩', 8);
        WIDTHS.put('╦', 8);
        WIDTHS.put('╠', 8);
        WIDTHS.put('═', 8);
        WIDTHS.put('╬', 8);
        WIDTHS.put('╧', 8);
        WIDTHS.put('╨', 8);
        WIDTHS.put('╤', 8);
        WIDTHS.put('╥', 8);
        WIDTHS.put('╙', 8);
        WIDTHS.put('╘', 8);
        WIDTHS.put('╒', 8);
        WIDTHS.put('╓', 8);
        WIDTHS.put('╫', 8);
        WIDTHS.put('╪', 8);
        WIDTHS.put('┘', 5);
        WIDTHS.put('┌', 8);
        WIDTHS.put('█', 8);
        WIDTHS.put('▄', 8);
        WIDTHS.put('▌', 4);
        WIDTHS.put('▐', 8);
        WIDTHS.put('▀', 8);
        WIDTHS.put('∅', 7);
        WIDTHS.put('∈', 5);
        WIDTHS.put('≡', 6);
        WIDTHS.put('±', 5);
        WIDTHS.put('≥', 5);
        WIDTHS.put('≤', 5);
        WIDTHS.put('⌠', 7);
        WIDTHS.put('⌡', 4);
        WIDTHS.put('÷', 5);
        WIDTHS.put('≈', 6);
        WIDTHS.put('°', 4);
        WIDTHS.put('∙', 5);
        WIDTHS.put('√', 6);
        WIDTHS.put('ⁿ', 4);
        WIDTHS.put('²', 4);
        WIDTHS.put('■', 5);
        WIDTHS.put('∞', 7);
        WIDTHS.put('\uE000', -1);
        WIDTHS.put('\uE001', -2);
        WIDTHS.put('\uE002', -4);
        WIDTHS.put('\uE003', -8);
        WIDTHS.put('\uE004', -16);
        WIDTHS.put('\uE005', -32);
        WIDTHS.put('\uE006', -64);
        WIDTHS.put('\uE007', -128);
        WIDTHS.put('\uF000', 1);
        WIDTHS.put('\uF001', 2);
        WIDTHS.put('\uF002', 4);
        WIDTHS.put('\uF003', 8);
        WIDTHS.put('\uF004', 16);
        WIDTHS.put('\uF005', 32);
        WIDTHS.put('\uF006', 64);
        WIDTHS.put('\uF007', 128);
    }

    public static int width(Component component) {
        if (component.children().isEmpty()) {
            if (component == Component.empty())
                return 0;

            if (component instanceof TextComponent textComponent) {
                return textComponentWidth(textComponent);
            } else if (component instanceof ObjectComponent objectComponent) {
                return objectComponentWidth(objectComponent);
            } else {
                throw new UnsupportedOperationException("Unsupported for component class " + component.getClass().getSimpleName());
            }
        }

        List<Component> components = getAllComponents(component);

        int componentsSize = components.size();
        int totalWidth = 0;

        for (int i = componentsSize - 1; i >= 0; i--) {
            Component c = components.get(i);

            if (c instanceof TextComponent textComponent) {
                if (textComponent.content().isEmpty())
                    continue;

                totalWidth += textComponentWidth(textComponent);
            } else if (c instanceof ObjectComponent objectComponent) {
                totalWidth += objectComponentWidth(objectComponent);
            }

            if (i != componentsSize - 1) {
                totalWidth++;
            }
        }

        return totalWidth;
    }

    private static List<Component> getAllComponents(Component component) {
        List<Component> components = getChildComponents(component);
        components.add(component);

        return components;
    }

    private static List<Component> getChildComponents(Component component) {
        List<Component> components = new ArrayList<>(component.children());

        for (Component child : component.children()) {
            if (child.children().isEmpty()) {
                continue;
            }
            
            components.addAll(getChildComponents(child));
        }

        return components;
    }

    private static int textComponentWidth(TextComponent component) {
        return width(component.content());
    }

    private static int objectComponentWidth(ObjectComponent component) {
        ObjectContents contents = component.contents();

        if (contents instanceof SpriteObjectContents)
            return 7; // this is always the width as far as i know

        // TODO: PlayerHead
        return 0;
    }

    public static int width(String string) {
        if (string.isEmpty())
            return 0;

        int totalWidth = 0;
        char[] chars = string.toCharArray();

        for (int i = 0; i < chars.length - 1; i++) {
            char c = chars[i];

            if (c == ' ')
                totalWidth += 4;
            else
                totalWidth += width(c) + 1;
        }

        char lastChar = chars[chars.length - 1];

        if (lastChar == ' ')
            totalWidth += 4;
        else
            totalWidth += width(lastChar);

        return totalWidth;
    }

    public static int width(char c) {
        if (!WIDTHS.containsKey(c))
            throw new IllegalArgumentException("character " + c + " does not have a mapped width");

        return WIDTHS.get(c);
    }

    private FontMetrics() {}

}
