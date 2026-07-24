package dev.zenqrt.bouncybullets.loadout.kit;

import dev.zenqrt.bouncybullets.event.EventNode;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public interface EventPlayerClass extends PlayerClass {

    EventNode<Event> registerEvents(BouncyBulletGame game);

    default boolean isPlayerClass(BouncyBulletGame game, Player player, PlayerClass playerClass) {
        return game.findPlayer(player.getUniqueId())
                .map(gamePlayer -> gamePlayer.getLoadout().classType().getPlayerClass() == playerClass)
                .orElse(false);
    }

    default boolean isPlayerClass(BouncyBulletGamePlayer gamePlayer, PlayerClass playerClass) {
        return gamePlayer.getLoadout().classType().getPlayerClass() == playerClass;
    }
}
