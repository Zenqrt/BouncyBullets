package dev.zenqrt.bouncybullets.game.games;

import org.bukkit.entity.Player;

import java.util.UUID;

public record BouncyBulletPlayer(UUID uuid, Player player, int kills, int deaths, Loadout loadout) {

    public BouncyBulletPlayer withKills(int kills) {
        return new BouncyBulletPlayer(uuid, player, kills, deaths, loadout);
    }

    public BouncyBulletPlayer addKill() {
        return withKills(this.kills + 1);
    }

    public BouncyBulletPlayer withDeaths(int deaths) {
        return new BouncyBulletPlayer(uuid, player, kills, deaths, loadout);
    }

    public BouncyBulletPlayer addDeath() {
        return withDeaths(this.deaths + 1);
    }

}
