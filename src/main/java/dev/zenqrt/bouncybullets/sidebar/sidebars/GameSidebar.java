package dev.zenqrt.bouncybullets.sidebar.sidebars;

import dev.zenqrt.bouncybullets.sidebar.PacketSidebar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

import java.util.List;

public final class GameSidebar extends PacketSidebar {

    public GameSidebar(int gameTimeSeconds, List<Player> startingPlayersTop) {
        super(Component.text("BOUNCY BULLETS", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));

        this.addLine("your_kills", yourKillsText(0));
        this.addEmptyLine();

        for (int i = Math.min(startingPlayersTop.size() - 1, 2); i >= 0; i--) {
            Player player = startingPlayersTop.get(i);
            int place = i + 1;

            this.addLine("place_" + place, placeText(place, getUsername(player), 0));
        }

        this.addLine("top_kills_header", Component.text("Top Kills:", NamedTextColor.WHITE)
                .decorate(TextDecoration.BOLD));

        this.addEmptyLine();
        this.addLine("game_time", gameTimeText(gameTimeSeconds));
    }

    public void setYourKills(int kills) {
        this.updateLine("your_kills", yourKillsText(kills));
    }

    public void setPlace(int place, Player player, int kills) {
        this.updateLine("place_" + place, placeText(place, getUsername(player), kills));
    }

    public void setGameTime(int gameTimeSeconds) {
        this.updateLine("game_time", gameTimeText(gameTimeSeconds));
    }

    private static Component getUsername(Player player) {
        return Component.text(player.getName(), NamedTextColor.AQUA);
    }

    private static Component yourKillsText(int kills) {
        return Component.text("Kills: ", NamedTextColor.WHITE)
                .append(Component.text(kills, NamedTextColor.GREEN));
    }

    private static Component placeText(int place, Component username, int kills) {
        return Component.text(place + ". ", NamedTextColor.WHITE)
                .append(username)
                .append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text(kills, NamedTextColor.YELLOW));
    }

    private static Component gameTimeText(int gameTimeSeconds) {
        int minutes = gameTimeSeconds / 60;
        int seconds = gameTimeSeconds % 60;

        return Component.text("Time Left: ", NamedTextColor.WHITE)
                .append(Component.text(minutes + ":%02d".formatted(seconds), NamedTextColor.GREEN));
    }

}
