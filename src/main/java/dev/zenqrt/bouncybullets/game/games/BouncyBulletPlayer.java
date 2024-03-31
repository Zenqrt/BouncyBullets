package dev.zenqrt.bouncybullets.game.games;

import dev.zenqrt.bouncybullets.game.games.kit.PlayerClass;
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

    public BouncyBulletPlayer withPlayerClass(PlayerClass playerClass) {
        return new BouncyBulletPlayer(uuid, player, kills, deaths, new Loadout(playerClass));
    }

}
