package dev.zenqrt.bouncybullets.utils;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

public final class Space {

    private static final int MAX_SPACE = 128;
    private static final Key NEGATIVE_SPACE_FONT = Key.key("negative_space", "default");

    public static Component of(int pixels) {
        if (pixels > 0)
            return positiveSpace(pixels);

        if (pixels < 0)
            return negativeSpace(-pixels);

        return Component.empty();
    }

    private static Component positiveSpace(int pixels) {
        if (pixels == 1)
            return Component.text('\uF000');

        StringBuilder builder = new StringBuilder();

        for (int increment = MAX_SPACE; increment > 0; increment /= 2) {
            while (pixels >= increment) {
                builder.append(positiveGlyph(increment));
                pixels -= increment;
            }
        }

        return Component.text(builder.toString())
                .font(NEGATIVE_SPACE_FONT);
    }

    private static Component negativeSpace(int pixels) {
        if (pixels == 1)
            return Component.text('\uE000');

        StringBuilder builder = new StringBuilder();

        for (int increment = MAX_SPACE; increment > 0; increment /= 2) {
            while (pixels >= increment) {
                builder.append(negativeGlyph(increment));
                pixels -= increment;
            }
        }

        return Component.text(builder.toString())
                .font(NEGATIVE_SPACE_FONT);
    }

    private static char negativeGlyph(int pixels) {
        return (char) + ('\uE000' + Integer.numberOfTrailingZeros(pixels));
    }

    private static char positiveGlyph(int pixels) {
        return (char) + ('\uF000' + Integer.numberOfTrailingZeros(pixels));
    }
}
