package dev.zenqrt.bouncybullets.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public final class AdventureUtilsTests {

    @Test
    @DisplayName("Should wrap text ignoring tags in character count")
    void shouldWrapTextIgnoringTagsInCharacterCount() {
        String text = "<gray>Upon killing a player, receive a <aqua>Speed II <gray>effect for <green>5 <gray>seconds.";

        String actual = AdventureUtils.wrapTextIgnoringTags(text, 30);
        String expected = "<gray>Upon killing a player, receive \na <aqua>Speed II <gray>effect for <green>5 <gray>\nseconds.";

        Assertions.assertEquals(expected, actual);
    }
}
