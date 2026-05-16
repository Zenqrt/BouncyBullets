package dev.zenqrt.bouncybullets.game.games.kit;

import dev.zenqrt.bouncybullets.event.impl.PaperEventNode;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import org.bukkit.event.Event;

public abstract class EventPlayerClass implements PlayerClass {

    protected final PaperEventNode<Event> eventNode = new PaperEventNode<>();

    public abstract void registerEvents(BouncyBulletGamePlayer player);

}
