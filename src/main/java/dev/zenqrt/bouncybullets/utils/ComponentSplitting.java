/*
 * This file is part of adventure, licensed under the MIT License.
 *
 * Copyright (c) 2017-2022 KyoriPowered
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package dev.zenqrt.bouncybullets.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.NotNull;

public final class ComponentSplitting {

    private ComponentSplitting() {
    }

    public static @NotNull List<Component> split(final @NotNull Component self, final @NotNull Pattern regex) {
        return new ArrayList<>(split0(self, Style.empty(), regex));
    }

    private static @NotNull Deque<Component> split0(final @NotNull Component self, final @NotNull Style parentStyle, final @NotNull Pattern regex) {
        final Deque<Component> parts = new LinkedList<>();
        final Style style = self.style().merge(parentStyle, Style.Merge.Strategy.IF_ABSENT_ON_TARGET);
        if (self instanceof TextComponent) {
            final String content = ((TextComponent) self).content();
            final String[] result = regex.split(content, -1);
            parts.addLast(Component.text(result[0], self.style()));
            for (int i = 1; i < result.length; i++) {
                // Create new component with the computed style for each string part
                parts.add(Component.text(result[i], style));
            }
        } else {
            // Remove the children to split them recursively
            parts.addLast(self.children(Collections.emptyList()));
        }

        boolean sibling = false;
        for (final Component child : self.children()) {
            final Deque<Component> result = split0(child, style, regex);
            // Append the first part to the last parent component
            // and remove empty components
            final Component root = parts.pollLast();
            final Component first = result.pollFirst();
            if (isEmpty(first)) {
                parts.addLast(root);
            } else if (isEmpty(root)) {
                final Style newStyle = first.style().merge(root.style(), Style.Merge.Strategy.IF_ABSENT_ON_TARGET);
                parts.addLast(first.style(newStyle));
                sibling = true;
            } else if (sibling) {
                parts.addLast(Component.empty().children(Arrays.asList(root, first)));
                sibling = false;
            } else {
                parts.addLast(root.append(first));
            }

            // Add the remaining parts
            if (parts.addAll(result)) {
                sibling = true;
            }
        }

        return parts;
    }

    /**
     * Tests if the component has no content nor children.
     *
     * @param component the component to test
     * @return {@code true} if empty
     */
    private static boolean isEmpty(final @NotNull Component component) {
        return component instanceof TextComponent
                && ((TextComponent) component).content().isEmpty()
                && component.children().isEmpty();
    }
}