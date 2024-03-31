package dev.zenqrt.bouncybullets.events;

import dev.zenqrt.bouncybullets.BouncyBullets;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletPlayer;
import dev.zenqrt.bouncybullets.game.games.Loadout;
import dev.zenqrt.bouncybullets.game.games.kit.StealthPlayerClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerJoinListeners implements Listener {

    @EventHandler
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        if (!BouncyBullets.getGame().canJoinGame()) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_FULL, Component.text("Sorry, the game is full! :(", NamedTextColor.RED));
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.joinMessage(null);

        Player player = event.getPlayer();
        BouncyBulletPlayer bouncyBulletPlayer = new BouncyBulletPlayer(
                player.getUniqueId(),
                player,
                0,
                0,
                new Loadout(new StealthPlayerClass())
        );
        BouncyBullets.getGame().insertPlayer(bouncyBulletPlayer);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.quitMessage(null);

        BouncyBullets.getGame().removePlayer(event.getPlayer().getUniqueId());
    }

}
