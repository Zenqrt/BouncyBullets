package dev.zenqrt.bouncybullets.game.games.states;

import dev.zenqrt.bouncybullets.event.EventNode;
import dev.zenqrt.bouncybullets.event.GameEventNodes;
import dev.zenqrt.bouncybullets.event.PaperEventListener;
import dev.zenqrt.bouncybullets.event.events.PlayerJoinGameEvent;
import dev.zenqrt.bouncybullets.game.base.GameStateSequence;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.item.GameItems;
import dev.zenqrt.bouncybullets.player.GamePlayerList;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.Inventory;

import java.util.List;

public final class PregameGameState extends GameStateSequence {

    private final EventNode<PlayerEvent> playerEventNode;

    private final GamePlayerList players;
    final BouncyBulletGame game;

    public PregameGameState(BouncyBulletGame game, GamePlayerList players) {
        this.game = game;
        this.players = players;

        this.playerEventNode = GameEventNodes.filteredPlayerEvents(game);

        this.states = List.of(
                new WaitingGameState(this, players, game.getGameSettings().minPlayers()),
                new CountdownGameState(this, players, game.getGameSettings().minPlayers())
        );
    }

    private void registerEvents() {

        this.playerEventNode.registerListener(PaperEventListener.builder(PlayerDropItemEvent.class)
                .handler(event -> event.setCancelled(true))
                .build());
        this.playerEventNode.registerListener(PaperEventListener.builder(PlayerJoinGameEvent.class)
                .handler(event -> {
                    players.sendMessage(MiniMessage.miniMessage().deserialize("<green>{player} joined the game! ({playerCount}/{maxPlayers})")
                            .replaceText(builder -> builder.matchLiteral("{player}").replacement(event.getPlayer().name()))
                            .replaceText(builder -> builder.matchLiteral("{playerCount}").replacement(String.valueOf(players.size())))
                            .replaceText(builder -> builder.matchLiteral("{maxPlayers}").replacement(String.valueOf(game.getGameSettings().maxPlayers()))));
                    setupPlayer(event.getPlayer());
                })
                .build());
    }

    @Override
    protected void onStateStart() {
        registerEvents();

        players.forEach(((uuid, gamePlayer) -> setupPlayer(gamePlayer.getPlayer())));

        super.onStateStart();
    }

    private void setupPlayer(Player player) {
        player.setGameMode(GameMode.ADVENTURE);
        player.teleport(this.game.getGameMap().intermissionSpawn());

        player.setLevel(0);
        player.setExp(0);
        player.setHealth(20);
        player.setFoodLevel(20);

        Inventory inventory = player.getInventory();
        inventory.clear();
        givePlayerItems(inventory);

    }

    private void givePlayerItems(Inventory inventory) {
        inventory.setItem(0, GameItems.LOADOUT.buildItemStack());
    }

    @Override
    protected void onLastStateFinished() {
        game.switchNextState();
    }

    @Override
    protected void onStateEnd() {
        super.onStateEnd();
        playerEventNode.unregisterAllListeners();
    }

    @Override
    public boolean canPlayersJoin() {
        return true;
    }
}
