package dev.zenqrt.bouncybullets.game.games;

import dev.zenqrt.bouncybullets.game.games.kit.PlayerClass;
import org.bukkit.entity.Player;

import java.util.UUID;

public record BouncyBulletGamePlayer(UUID uuid, Player player, int kills, int deaths, Loadout loadout) {

    public BouncyBulletGamePlayer withKills(int kills) {
        return new BouncyBulletGamePlayer(uuid, player, kills, deaths, loadout);
    }

    public BouncyBulletGamePlayer addKill() {
        return withKills(this.kills + 1);
    }

    public BouncyBulletGamePlayer withDeaths(int deaths) {
        return new BouncyBulletGamePlayer(uuid, player, kills, deaths, loadout);
    }

    public BouncyBulletGamePlayer addDeath() {
        return withDeaths(this.deaths + 1);
    }

    public BouncyBulletGamePlayer withPlayerClass(PlayerClass playerClass) {
        return new BouncyBulletGamePlayer(uuid, player, kills, deaths, new Loadout(playerClass));
    }

}
