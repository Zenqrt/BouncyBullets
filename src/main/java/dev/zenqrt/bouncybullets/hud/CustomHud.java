package dev.zenqrt.bouncybullets.hud;


import dev.zenqrt.bouncybullets.utils.FontMetrics;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/*
 * Store each hud element with
 *  - horizontal position, hud alignment
 * Should be able to display HUDs in the boss bar, title/subtitle, and action bar
 *  - Keep separate collections for each, each collection storing the hud element data
 *
 * HudComponent mutable class:
 *  - Stores horizontalOffset, hudElement
 *
 * HudElement interface:
 *  - Component render()
 *      - The ending position of a component should be at the end of the longest text
 *          - e.g. "12345" -> the ending position should be at the end of "5"
 *
 * HudAlignment:
 *  - CENTER, LEFT, RIGHT
 *
 * CustomHud class:
 *  - void render()
 *      - Handles rendering action bar, title/subtitle, and boss bar displays
 *  - LinkedList<HudComponent> actionBarComponents ; O(n) to sorted insert
 *  - ArrayList<LinkedList<HudComponent>> bossBarComponents
 *
 * Center text rendering algorithm:
 * 1. Build string with offsets from left to right (this should still have the string centered after building it)
 *      a. Display each HudElement and calculate the in between space between the next element (if exist) and add the spaces
 *          - O(n)
 *      b. Before a., the collection should already be sorted
 * 2. Add offsets to put components in proper positions
 *      a. Using the center-most element (closest the center) should be used to calculate this offset
 *
 * Example: "AAA" - 4 spaces - RIGHT ALIGN ; "BB" - CENTER ALIGN
 * ----------
 * AAA:
 *
 * width = 3
 *
 */
public class CustomHud {

    private static int calculateSpaceOffset(HudComponent hudComponent, Component render) {
        return switch (hudComponent.getHudAlignment()) {
            case CENTER -> hudComponent.getHorizontalOffset();
            case LEFT -> {
                int width = FontMetrics.width(render);
                yield hudComponent.getHorizontalOffset() - width;
            }
            case RIGHT -> {
                int width = FontMetrics.width(render);
                yield hudComponent.getHorizontalOffset() + width;
            }
        };
    }

    // Should always be sorted from left to right elements
    private final List<HudComponent> actionBarComponents = new LinkedList<>();

    public final void render() {
        TextComponent.Builder builder = Component.text();

        // Stage 1: Build string with offsets from left to right
        ListIterator<HudComponent> iterator = this.actionBarComponents.listIterator();
        HudComponent previous = null;

        while (iterator.hasNext()) {
            HudComponent current = iterator.next();
            HudComponent next = iterator.next();

            Component currentRender = current.getHudElement().render();
            int currentOffset = calculateSpaceOffset(current, currentRender);

            if (previous != null) {
                Component previousRender = previous.getHudElement().render();
                int previousOffset = calculateSpaceOffset(previous, previousRender);

                int distanceToPrevious = currentOffset - previousOffset;

                for (int i = 0; i < distanceToPrevious; i++) {
                    builder.append(Component.space());
                }
            }

            builder.append(currentRender);

            Component nextRender = next.getHudElement().render();
            int nextOffset = calculateSpaceOffset(next, nextRender);

            int distanceToNext = nextOffset - currentOffset;

            for (int i = 0; i < distanceToNext; i++) {
                builder.append(Component.space());
            }

            builder.append(nextRender);

            previous = next;
        }

        // TODO: Stage 2: Aligning component to ensure all hud elements are in correct positions
    }

    public final void addComponentAtEnd(HudComponent hudComponent) {

    }

    public final void addComponentAt(int index, HudComponent hudComponent) {

    }

}
