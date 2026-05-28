package dev.zenqrt.bouncybullets.stats;

import dev.zenqrt.bouncybullets.loadout.kit.PlayerClassType;

import java.util.EnumMap;

public final class PlayerStats {

    private int gamesPlayed;
    private int totalKills;
    private int totalDeaths;
    private int totalWins;
    private int totalLosses;

    private final EnumMap<PlayerClassType, PlayerClassStats> playerClassStats = new EnumMap<>(PlayerClassType.class);

    public PlayerStats() {
        this.gamesPlayed = 0;
        this.totalKills = 0;
        this.totalDeaths = 0;
        this.totalWins = 0;
        this.totalLosses = 0;
    }

    public void addGamesPlayed() {
        this.gamesPlayed++;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void addKillToTotal() {
        this.totalKills++;
    }

    public int getTotalKills() {
        return totalKills;
    }

    public void addDeathToTotal() {
        this.totalDeaths++;
    }

    public int getTotalDeaths() {
        return totalDeaths;
    }

    public void addWinToTotal() {
        this.totalWins++;
    }

    public int getTotalWins() {
        return totalWins;
    }

    public void addLossToTotal() {
        this.totalLosses++;
    }

    public int getTotalLosses() {
        return totalLosses;
    }

    public float getTotalKillDeathRatio() {
        if (this.totalDeaths <= 0)
            return this.totalKills;

        return (float) this.totalKills / this.totalDeaths;
    }

    public float getTotalWinLossRatio() {
        if (this.totalLosses <= 0)
            return this.totalWins;

        return (float) this.totalWins / this.totalLosses;
    }

    public PlayerClassStats getClassStats(PlayerClassType classType) {
        return this.playerClassStats
                .computeIfAbsent(classType, _ -> new PlayerClassStats());
    }
}
