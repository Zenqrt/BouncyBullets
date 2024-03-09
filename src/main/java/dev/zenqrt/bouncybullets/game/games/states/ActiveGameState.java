package dev.zenqrt.bouncybullets.game.games.states;

import dev.zenqrt.bouncybullets.game.games.BouncyBulletPlayer;
import dev.zenqrt.bouncybullets.game.impl.PaperGameEventHandler;
import dev.zenqrt.bouncybullets.game.impl.PaperGameState;
import dev.zenqrt.bouncybullets.item.GameItem;
import dev.zenqrt.bouncybullets.item.items.PistolGunItem;

import java.util.Map;
import java.util.UUID;

public final class ActiveGameState extends PaperGameState {

    private static final PistolGunItem PISTOL_ITEM = new PistolGunItem();

    private final Map<UUID, BouncyBulletPlayer> players;

    public ActiveGameState(PaperGameEventHandler eventHandler, Map<UUID, BouncyBulletPlayer> players) {
        super(eventHandler);

        this.players = players;
    }

    @Override
    public void registerEvents() {
        GameItem.registerGameItemEvents(eventHandler, PISTOL_ITEM);
    }

    @Override
    protected void onStateStart() {
        players.forEach((uuid, player) -> player.player().getInventory().addItem(PISTOL_ITEM.buildItemStack()));
    }
}
