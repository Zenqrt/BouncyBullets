package dev.zenqrt.bouncybullets.game.games.kit;

import dev.zenqrt.bouncybullets.game.event.impl.PaperEventNode;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletPlayer;
import org.bukkit.event.Event;

public abstract class EventPlayerClass implements PlayerClass {

    protected final PaperEventNode<Event> eventNode = new PaperEventNode<>();

    public abstract void registerEvents(BouncyBulletPlayer player);

}
