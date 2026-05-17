package dev.zenqrt.bouncybullets.game.games.states;

import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent;
import dev.zenqrt.bouncybullets.event.EventNode;
import dev.zenqrt.bouncybullets.event.GameEventNodes;
import dev.zenqrt.bouncybullets.event.PaperEventListener;
import dev.zenqrt.bouncybullets.game.base.GameState;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import dev.zenqrt.bouncybullets.loadout.Loadout;
import dev.zenqrt.bouncybullets.loadout.kit.EventPlayerClass;
import dev.zenqrt.bouncybullets.loadout.kit.PlayerClass;
import dev.zenqrt.bouncybullets.map.FreeForAllActiveGameMap;
import dev.zenqrt.bouncybullets.player.GamePlayerList;
import dev.zenqrt.bouncybullets.utils.TaskManager;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.time.Duration;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public final class BattleGameState extends GameState {

    private static final Sound KILL_SOUND = Sound.sound(org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, Sound.Source.MASTER, 1, 1);
    private static final AttributeModifier NO_KNOCKBACK_MODIFIER = new AttributeModifier("bouncy-bullets_no_kb", 100, AttributeModifier.Operation.ADD_NUMBER);
    private static final AttributeModifier GAME_SPEED_MODIFIER = new AttributeModifier("bouncy-bullets_game_speed", 0.04, AttributeModifier.Operation.ADD_NUMBER);

    private final EventNode<PlayerEvent> playerEventNode;
    private final EventNode<EntityEvent> playerEntityEventNode;

    private final TaskManager taskManager;
    private final BouncyBulletGame game;
    private final GamePlayerList players;
    private final FreeForAllActiveGameMap gameMap;

    public BattleGameState(BouncyBulletGame game, GamePlayerList players, FreeForAllActiveGameMap gameMap) {
        this.game = game;
        this.players = players;
        this.gameMap = gameMap;
        this.taskManager = new TaskManager(game.getPlugin());

        this.playerEventNode = GameEventNodes.filteredPlayerEvents(game);
        this.playerEntityEventNode = GameEventNodes.filteredEntityEvents(game);
    }

    @Override
    protected void onStateStart() {
        super.onStateStart();

        registerEvents();

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        scoreboard.getTeams().forEach(Team::unregister);

        Team team = scoreboard.registerNewTeam("players");
        team.setCanSeeFriendlyInvisibles(false);

        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);

        this.players.forEach((uuid, gamePlayer) -> {
            Player player = gamePlayer.getPlayer();
            AttributeInstance knockbackResistance = Objects.requireNonNull(
                    player.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE),
                    "knockbackResistance"
            );
            AttributeInstance movementSpeed = Objects.requireNonNull(
                    player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED),
                    "movementSpeed"
            );

            knockbackResistance.addTransientModifier(NO_KNOCKBACK_MODIFIER);
            movementSpeed.addTransientModifier(GAME_SPEED_MODIFIER);

            team.addPlayer(player);
            setupPlayerInventory(player, gamePlayer.getLoadout());

            Location randomSpawn = gameMap.spawnLocations().get(ThreadLocalRandom.current().nextInt(gameMap.spawnLocations().size())).toLocation(gameMap.world());
            player.teleport(randomSpawn);
        });

        this.taskManager.runTaskTimer(
                new GameTimerTask(this.game.getGameSettings().gameTime()),
                0, 20
        );
        this.taskManager.runTaskTimer(
                new HUDTask(),
                0, 1
        );
    }

    @Override
    protected void onStateEnd() {
        super.onStateEnd();

        this.taskManager.removeAllTasks();

        this.players.forEach((uuid, gamePlayer) -> {
            gamePlayer.getLoadout().playerClass().onStopUse(gamePlayer);

            Player player = gamePlayer.getPlayer();

            AttributeInstance knockbackResistance = Objects.requireNonNull(
                    player.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE),
                    "knockbackResistance"
            );
            AttributeInstance movementSpeed = Objects.requireNonNull(
                    player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED),
                    "movementSpeed"
            );

            knockbackResistance.removeModifier(NO_KNOCKBACK_MODIFIER);
            movementSpeed.removeModifier(GAME_SPEED_MODIFIER);

            player.getInventory().clear();
            player.setGameMode(GameMode.SPECTATOR);
            player.clearActivePotionEffects();
            player.sendActionBar(Component.empty());
        });
    }

    @SuppressWarnings("UnstableApiUsage")
    private void registerEvents() {
        this.players.forEach((uuid, gamePlayer) -> {
            PlayerClass playerClass = gamePlayer.getLoadout().playerClass();

            if (playerClass instanceof EventPlayerClass eventPlayerClass) {
                eventPlayerClass.registerEvents(this.game);
            }

            playerClass.onStartUse(gamePlayer);
        });
        this.playerEventNode.registerListener(PaperEventListener.builder(PlayerTeleportEvent.class)
                .filter(event -> event.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE)
                .handler(event ->  {
                    event.setCancelled(true);

                    Entity spectatorTarget = event.getPlayer().getSpectatorTarget();
                    if (spectatorTarget != null) {
                        event.getPlayer().setSpectatorTarget(spectatorTarget);
                    }
                })
                .build());
        this.playerEventNode.registerListener(PaperEventListener.builder(PlayerStopSpectatingEntityEvent.class)
                .filter(event -> event.getPlayer().getGameMode() == GameMode.SPECTATOR)
                .handler(event -> event.setCancelled(true))
                .build());
        this.playerEntityEventNode.registerListener(PaperEventListener.builder(PlayerDeathEvent.class)
                .filter(event -> players.containsKey(event.getPlayer().getUniqueId()))
                .handler(event -> {
                    event.setCancelled(true);

                    Player player = event.getPlayer();

                    player.setGameMode(GameMode.SPECTATOR);
                    player.showTitle(Title.title(Component.text("YOU DIED!", NamedTextColor.RED).decorate(TextDecoration.BOLD), Component.empty(),
                            Title.Times.times(Duration.ZERO, Duration.ofSeconds(5), Duration.ZERO)));
                    player.clearActivePotionEffects();

                    EntityDamageEvent lastDamageEvent = player.getLastDamageCause();

                    if (lastDamageEvent != null) {
                        if (lastDamageEvent.getDamageSource().getCausingEntity() instanceof Player killer && players.containsKey(killer.getUniqueId())) {
                            BouncyBulletGamePlayer killerGamePlayer = this.game.findPlayer(killer.getUniqueId());

                            killerGamePlayer.addKill();
                            player.setSpectatorTarget(killer);

                            killer.playSound(KILL_SOUND, Sound.Emitter.self());
                            players.sendMessage(Component.text(player.getName() + " \uD83D\uDD2B " + killer.getName(), NamedTextColor.RED));
                        }
                    }

                    BouncyBulletGamePlayer gamePlayer = this.game.findPlayer(player.getUniqueId());
                    gamePlayer.addDeath();

                    this.taskManager.runTaskTimer(
                            new DeathSpectatorTask(gamePlayer, 5),
                            0, 20
                    );
                })
                .build());
//        this.eventNode.registerListener(PaperEventListener.builder(InventoryClickEvent.class)
//                .filter(event -> players.containsKey(event.getWhoClicked().getUniqueId()))
//                .handler(event -> event.setCancelled(true))
//                .build());
    }

    private Location chooseBestSpawnLocation() {
        return gameMap.spawnLocations().stream()
                .map(location -> location.toLocation(gameMap.world()))
                .max(Comparator.comparing(this::closestDistanceToPlayer))
                .orElseThrow();
    }

    private double closestDistanceToPlayer(Location location) {
        return players.values().stream()
                .mapToDouble(gamePlayer -> location.distance(gamePlayer.getPlayer().getLocation()))
                .min()
                .orElse(0);
    }

    private static void setupPlayerInventory(Player player, Loadout loadout) {
        PlayerInventory inventory = player.getInventory();
        inventory.clear();

        loadout.giveItems(inventory);
    }

    private final class GameTimerTask implements Runnable {

        private int timeLeft;

        GameTimerTask(int time) {
            this.timeLeft = time;
        }

        @Override
        public void run() {
            if (--timeLeft == 0) {
                game.switchNextState();
            }
        }
    }

    private final class HUDTask implements Runnable {

        @Override
        public void run() {
            for (BouncyBulletGamePlayer gamePlayer : BattleGameState.this.game.getPlayers().values()) {
                gamePlayer.getHud().show(gamePlayer.getPlayer());
            }
        }
    }

    private final class DeathSpectatorTask extends BukkitRunnable {

        private final BouncyBulletGamePlayer gamePlayer;
        private int timeLeft;

        DeathSpectatorTask(BouncyBulletGamePlayer gamePlayer, int respawnTimeSecs) {
            this.gamePlayer = gamePlayer;
            this.timeLeft = respawnTimeSecs;
        }

        @Override
        public void run() {
            Player player = gamePlayer.getPlayer();

            if (--timeLeft == 0) {
                this.cancel();

                setupPlayerInventory(player, gamePlayer.getLoadout());
                player.setGameMode(GameMode.ADVENTURE);
                player.teleport(chooseBestSpawnLocation());
                player.clearTitle();
                return;
            }

            player.sendTitlePart(TitlePart.SUBTITLE, Component.text("Respawning in " + timeLeft + "..."));
        }
    }
}
