package dev.zenqrt.bouncybullets.stats;

public final class PlayerClassStats {

    private int kills;
    private int deaths;
    private int wins;
    private int losses;

    public void addKill() {
        this.kills++;
    }

    public int getKills() {
        return kills;
    }

    public void addDeath() {
        this.deaths++;
    }

    public int getDeaths() {
        return deaths;
    }

    public void addWin() {
        this.wins++;
    }

    public int getWins() {
        return wins;
    }

    public void addLoss() {
        this.losses++;
    }

    public int getLosses() {
        return losses;
    }
}
