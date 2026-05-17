package dev.zenqrt.bouncybullets.game.games;

import dev.zenqrt.bouncybullets.loadout.Loadout;
import dev.zenqrt.bouncybullets.player.BouncyBulletsHUD;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BouncyBulletGamePlayer {

    private int deaths;
    private int kills;

    private final BouncyBulletsHUD hud;
    private Loadout loadout;
    private final Player player;
    private final UUID uuid;

    public BouncyBulletGamePlayer(Player player, Loadout loadout) {
        this.uuid = player.getUniqueId();
        this.player = player;
        this.loadout = loadout;
        this.hud = new BouncyBulletsHUD();

        this.kills = 0;
        this.deaths = 0;
    }

    public void addDeath() {
        this.deaths++;
    }

    public int getDeaths() {
        return deaths;
    }

    public void addKill() {
        this.kills++;
    }

    public int getKills() {
        return kills;
    }

    public BouncyBulletsHUD getHud() {
        return hud;
    }

    public void setLoadout(Loadout loadout) {
        this.loadout = loadout;
    }

    public Loadout getLoadout() {
        return loadout;
    }

    public Player getPlayer() {
        return player;
    }

    public UUID getUuid() {
        return uuid;
    }
}
