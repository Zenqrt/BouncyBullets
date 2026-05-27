package dev.zenqrt.bouncybullets.sidebar.sidebars;

import dev.zenqrt.bouncybullets.sidebar.PacketSidebar;
import dev.zenqrt.bouncybullets.stats.PlayerStats;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.text.DecimalFormat;

public final class LobbySidebar extends PacketSidebar {

    private static final DecimalFormat SHORT_DECIMAL_FORMAT = new DecimalFormat("0.00");
    /*
    BOUNCY BULLETS
    ----------------
    Total Kills: 0
    Total Wins: 0

    KDR: 0.0
    WLR: 0.0

    www.website.com
     */
    public LobbySidebar(PlayerStats stats) {
        super(Component.text("BOUNCY BULLETS", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));

        this.addLine("footer", Component.text("Dev server", NamedTextColor.DARK_GRAY).decorate(TextDecoration.ITALIC));
        this.addEmptyLine();
        this.addLine("how_to_play_2", Component.text("/bb autojoin", NamedTextColor.GOLD));
        this.addLine("how_to_play_1", Component.text("To play, run ", NamedTextColor.GRAY));
        this.addEmptyLine();
        this.addLine("win_loss_ratio", winLossRatioText(stats.getTotalWinLossRatio()));
        this.addLine("kill_death_ratio", killDeathRatioText(stats.getTotalKillDeathRatio()));
        this.addEmptyLine();
        this.addLine("total_wins", totalWinsText(stats.getTotalWins()));
        this.addLine("total_kills", totalKillsText(stats.getTotalKills()));
        this.addEmptyLine();
    }

    private static Component winLossRatioText(float winLossRatio) {
        return Component.text("WLR: ", NamedTextColor.WHITE)
                .append(Component.text(SHORT_DECIMAL_FORMAT.format(winLossRatio), NamedTextColor.AQUA));
    }

    private static Component killDeathRatioText(float killDeathRatio) {
        return Component.text("KDR: ", NamedTextColor.WHITE)
                .append(Component.text(SHORT_DECIMAL_FORMAT.format(killDeathRatio), NamedTextColor.RED));
    }

    private static Component totalWinsText(int totalWins) {
        return Component.text("Total Wins: ", NamedTextColor.WHITE)
                .append(Component.text(totalWins, NamedTextColor.GREEN));
    }

    private static Component totalKillsText(int totalKills) {
        return Component.text("Total Kills: ", NamedTextColor.WHITE)
                .append(Component.text(totalKills, NamedTextColor.GREEN));
    }

}
