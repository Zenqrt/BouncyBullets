package dev.zenqrt.bouncybullets.sidebar.sidebars;

import dev.zenqrt.bouncybullets.sidebar.PacketSidebar;
import dev.zenqrt.bouncybullets.stats.PlayerStats;
import dev.zenqrt.bouncybullets.utils.AdventureUtils;
import dev.zenqrt.bouncybullets.utils.DecimalFormats;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class LobbySidebar extends PacketSidebar {

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
        super(MiniMessage.miniMessage().deserialize("<shadow:#000000FF><gradient:light_purple:#4281ff><b>BOUNCY BULLETS"));

        this.addLine("footer", AdventureUtils.withShadow("Dev server", NamedTextColor.DARK_GRAY).decorate(TextDecoration.ITALIC));
        this.addEmptyLine();
        this.addLine("how_to_play_2", AdventureUtils.withShadow("/bb game join", NamedTextColor.LIGHT_PURPLE));
        this.addLine("how_to_play_1", AdventureUtils.withShadow("To play, run:", NamedTextColor.GRAY));
        this.addEmptyLine();
        this.addLine("win_loss_ratio", winLossRatioText(stats.getTotalWinLossRatio()));
        this.addLine("kill_death_ratio", killDeathRatioText(stats.getTotalKillDeathRatio()));
        this.addEmptyLine();
        this.addLine("total_wins", totalWinsText(stats.getTotalWins()));
        this.addLine("total_kills", totalKillsText(stats.getTotalKills()));
        this.addEmptyLine();
//        this.addLine("title_separator", AdventureUtils.withShadow("                         ", NamedTextColor.DARK_GRAY).decorate(TextDecoration.STRIKETHROUGH));
    }

    private static Component winLossRatioText(float winLossRatio) {
        return AdventureUtils.withShadow("WLR: ", NamedTextColor.WHITE)
                .append(Component.text(DecimalFormats.SHORT_DECIMAL_FORMAT_2.format(winLossRatio), NamedTextColor.AQUA));
    }

    private static Component killDeathRatioText(float killDeathRatio) {
        return AdventureUtils.withShadow("KDR: ", NamedTextColor.WHITE)
                .append(Component.text(DecimalFormats.SHORT_DECIMAL_FORMAT_2.format(killDeathRatio), NamedTextColor.RED));
    }

    private static Component totalWinsText(int totalWins) {
        return AdventureUtils.withShadow("Total Wins: ", NamedTextColor.WHITE)
                .append(Component.text(totalWins, NamedTextColor.GREEN));
    }

    private static Component totalKillsText(int totalKills) {
        return AdventureUtils.withShadow("Total Kills: ", NamedTextColor.WHITE)
                .append(Component.text(totalKills, NamedTextColor.GREEN));
    }

}
