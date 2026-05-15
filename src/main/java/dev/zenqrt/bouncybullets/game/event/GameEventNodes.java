package dev.zenqrt.bouncybullets.game.event;

import dev.zenqrt.bouncybullets.game.event.impl.PaperEventNode;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.player.PlayerEvent;

public final class GameEventNodes {

    public static EventNode<PlayerEvent> filteredPlayerEvents(BouncyBulletGame game) {
        EventNode<PlayerEvent> node = new PaperEventNode<>();
        node.addGlobalFilter(event -> game.hasPlayer(event.getPlayer().getUniqueId()));

        return node;
    }

    public static EventNode<EntityEvent> filteredEntityEvents(BouncyBulletGame game) {
        EventNode<EntityEvent> node = new PaperEventNode<>();
        node.addGlobalFilter(event -> game.hasPlayer(event.getEntity().getUniqueId()));

        return node;
    }

}
