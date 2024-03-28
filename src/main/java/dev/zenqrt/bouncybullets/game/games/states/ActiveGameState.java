package dev.zenqrt.bouncybullets.game.games.states;

import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent;
import dev.zenqrt.bouncybullets.BouncyBullets;
import dev.zenqrt.bouncybullets.game.event.impl.PaperEventListener;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletPlayer;
import dev.zenqrt.bouncybullets.game.games.Gun;
import dev.zenqrt.bouncybullets.game.games.Loadout;
import dev.zenqrt.bouncybullets.game.impl.PaperGameState;
import dev.zenqrt.bouncybullets.item.GameItem;
import dev.zenqrt.bouncybullets.map.FreeForAllGameMap;
import dev.zenqrt.bouncybullets.player.GamePlayerList;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.Comparator;
import java.util.stream.Stream;

public final class ActiveGameState extends PaperGameState {

    private static final int GAME_TIME = 300; // 5 minutes
    private static final Sound KILL_SOUND = Sound.sound(org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.MASTER, 1, 1);

    private final BouncyBulletGame game;
    private final GamePlayerList players;
    private final FreeForAllGameMap gameMap;
    private final World world;

    public ActiveGameState(BouncyBulletGame game, GamePlayerList players, FreeForAllGameMap gameMap) {
        this.game = game;
        this.players = players;
        this.gameMap = gameMap;
        this.world = Bukkit.getWorld("game_world_" + game.getId());
    }

    @Override
    public void registerEvents() {
        GameItem.registerGameItemEvents(eventNode, Stream.of(Gun.values())
                .map(gun -> (GameItem) gun.getItem())
                .toList()
        );
        this.eventNode.registerListener(PaperEventListener.builder(PlayerTeleportEvent.class)
                .filter(event -> event.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE)
                .handler(event ->  {
                    event.setCancelled(true);

                    Entity spectatorTarget = event.getPlayer().getSpectatorTarget();
                    if (spectatorTarget != null) {
                        event.getPlayer().setSpectatorTarget(spectatorTarget);
                    }
                })
                .build());
        this.eventNode.registerListener(PaperEventListener.builder(PlayerStopSpectatingEntityEvent.class)
                .filter(event -> event.getPlayer().getGameMode() == GameMode.SPECTATOR)
                .handler(event -> event.setCancelled(true))
                .build());
        this.eventNode.registerListener(PaperEventListener.builder(PlayerDeathEvent.class)
                .filter(event -> players.containsKey(event.getPlayer().getUniqueId()))
                .handler(event -> {
                    event.setCancelled(true);

                    Player player = event.getPlayer();
                    player.setGameMode(GameMode.SPECTATOR);
                    player.showTitle(Title.title(Component.text("YOU DIED!", NamedTextColor.RED).decorate(TextDecoration.BOLD), Component.empty(),
                            Title.Times.times(Duration.ZERO, Duration.ofSeconds(2), Duration.ofSeconds(1))));
                    BouncyBulletPlayer updatedPlayer = players.updatePlayer(player.getUniqueId(), BouncyBulletPlayer::addDeath);

                    EntityDamageEvent lastDamageEvent = player.getLastDamageCause();

                    if (lastDamageEvent != null) {
                        if (lastDamageEvent.getDamageSource().getCausingEntity() instanceof Player killer && players.containsKey(killer.getUniqueId())) {
                            players.updatePlayer(killer.getUniqueId(), BouncyBulletPlayer::addKill);
                            player.setSpectatorTarget(killer);

                            killer.playSound(KILL_SOUND, Sound.Emitter.self());
                        }
                    }

                    new DeathSpectatorTask(updatedPlayer, 5)
                            .runTaskTimer(BouncyBullets.getInstance(), 0, 20);
                })
                .build());
    }

    private Location chooseBestSpawnLocation() {
        return gameMap.spawnLocations().stream()
                .map(location -> location.toLocation(world))
                .min(Comparator.comparing(this::closestDistanceToPlayer))
                .orElseThrow();
    }

    private double closestDistanceToPlayer(Location location) {

        return players.values().stream()
                .mapToDouble(player -> location.distance(player.player().getLocation()))
                .min()
                .orElse(0);
    }

    @Override
    protected void onStateStart() {
        players.forEach((uuid, player) -> {
            setupPlayer(player.player(), player.loadout());
            player.player().teleport(gameMap.spawnLocations().get(0).toLocation(world));
        });

        new GameTimerTask(GAME_TIME).runTaskTimer(BouncyBullets.getInstance(), 0, 20);
    }

    private static void setupPlayer(Player player, Loadout loadout) {
        PlayerInventory inventory = player.getInventory();
        inventory.clear();

        inventory.setItem(0, loadout.gun().buildItemStack());
    }

    private final class GameTimerTask extends BukkitRunnable {

        private int timeLeft;

        GameTimerTask(int time) {
            this.timeLeft = time;
        }

        @Override
        public void run() {
            if (--timeLeft == 0) {
                this.cancel();
                game.switchGameState(new EndingGameState(players));
            }
        }
    }

    private final class DeathSpectatorTask extends BukkitRunnable {

        private final BouncyBulletPlayer player;
        private int timeLeft;

        DeathSpectatorTask(BouncyBulletPlayer player, int respawnTime) {
            this.player = player;
            this.timeLeft = respawnTime;
        }

        @Override
        public void run() {
            Player bukkitPlayer = player.player();

            if (--timeLeft == 0) {
                this.cancel();

                setupPlayer(bukkitPlayer, player.loadout());
                bukkitPlayer.setGameMode(GameMode.ADVENTURE);
                bukkitPlayer.teleport(chooseBestSpawnLocation());
                bukkitPlayer.clearTitle();
                return;
            }

            bukkitPlayer.sendActionBar(Component.text("Respawning in " + timeLeft + "..."));
        }
    }
}
