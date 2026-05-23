package dev.zenqrt.bouncybullets.loadout.kit;

import dev.zenqrt.bouncybullets.event.PaperEventListener;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import dev.zenqrt.bouncybullets.item.GameItems;
import dev.zenqrt.bouncybullets.item.items.abilities.ActiveAbilityItem;
import dev.zenqrt.bouncybullets.item.items.guns.GunItem;
import dev.zenqrt.bouncybullets.player.BouncyBulletsHUD;
import dev.zenqrt.bouncybullets.utils.ItemUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class StealthPlayerClass extends EventPlayerClass {

    private static final int INVIS_CHARGE_TICKS = 100;      // 5 seconds
    private static final int COMBAT_TIMER_SECONDS = 8;
    private static final Sound HIDE_SOUND = Sound.sound(Key.key("entity.generic.drink"), Sound.Source.PLAYER, 0.5F, 1);
    private static final Sound REVEAL_SOUND = Sound.sound(Key.key("block.lava.extinguish"), Sound.Source.PLAYER, 0.5F, 2);
    private static final Sound CHARGE_DENIED_SOUND = Sound.sound(Key.key("entity.villager.no"), Sound.Source.MASTER, 1, 1.25F);

    private static final GunItem PRIMARY_GUN = GameItems.SMG;
    private static final GunItem SECONDARY_GUN = GameItems.SILENCED_PISTOL;
    private static final ActiveAbilityItem ACTIVE_ABILITY = GameItems.STEALTH_ACTIVE_ABILITY;

    private final Map<UUID, InvisibilityChargeTask> invisChargeTasks = new HashMap<>();
    private final Map<UUID, CombatTimerTask> combatTimerTasks = new HashMap<>();

    @Override
    public String getName() {
        return "Stealth";
    }

    @Override
    public Map<Integer, ItemStack> getItems() {
        return Map.of(
                0, PRIMARY_GUN.buildItemStack(),
                1, SECONDARY_GUN.buildItemStack(),
                2, ACTIVE_ABILITY.buildItemStack()
        );
    }

    @Override
    public Map<EquipmentSlot, ItemStack> getArmorEquipment() {
        return Map.of(
                EquipmentSlot.HEAD, ItemUtils.createLeatherArmor(Material.LEATHER_HELMET, Color.BLACK),
                EquipmentSlot.LEGS, new ItemStack(Material.CHAINMAIL_LEGGINGS),
                EquipmentSlot.FEET, ItemUtils.createLeatherArmor(Material.LEATHER_BOOTS, Color.fromRGB(43, 43, 43))
        );
    }

    @Override
    public void onStopUse(BouncyBulletGamePlayer gamePlayer) {
        UUID uuid = gamePlayer.getUuid();

        InvisibilityChargeTask invisChargeTask = this.invisChargeTasks.remove(uuid);

        if (invisChargeTask != null)
            invisChargeTask.cancel();

        CombatTimerTask combatTimerTask = this.combatTimerTasks.remove(uuid);

        if (combatTimerTask != null)
            combatTimerTask.cancel();

    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void registerEvents(BouncyBulletGame game) {
        this.eventNode.registerListener(PaperEventListener.builder(PlayerToggleSneakEvent.class)
                .filter(event -> isPlayerClass(game, event.getPlayer(), this))
                .handler(event -> {
                    Player player = event.getPlayer();
                    UUID uuid = player.getUniqueId();
                    BouncyBulletGamePlayer gamePlayer = game.findPlayer(uuid);

                    if (event.isSneaking() && canGoInvisible(gamePlayer)) {
                        if (this.combatTimerTasks.containsKey(uuid) || player.isSprinting()) {
                            player.sendMessage(Component.text("You are currently in combat!", NamedTextColor.RED));
                            player.playSound(CHARGE_DENIED_SOUND, Sound.Emitter.self());
                            return;
                        }

                        startInvisibilityCharge(gamePlayer, player, game.getPlugin());
                    } else {
                        cancelInvisibilityCharge(player);
                    }
                })
                .build()
        );
        this.eventNode.registerListener(PaperEventListener.builder(PlayerToggleSprintEvent.class)
                .filter(event -> isPlayerClass(game, event.getPlayer(), this))
                .filter(PlayerToggleSprintEvent::isSprinting)
                .handler(event -> {
                    Player player = event.getPlayer();
                    BouncyBulletGamePlayer gamePlayer = game.findPlayer(player.getUniqueId());

                    if (!gamePlayer.isInvisible())
                        return;

                    revealPlayer(gamePlayer, player);
                })
                .build()
        );
        this.eventNode.registerListener(PaperEventListener.builder(EntityDamageEvent.class, EventPriority.MONITOR)
                .filter(event -> !event.isCancelled())
                .filter(event -> event.getEntity() instanceof Player player && isPlayerClass(game, player, this))
                .handler(event -> {
                    Player player = (Player) event.getEntity();
                    UUID uuid = player.getUniqueId();
                    BouncyBulletGamePlayer gamePlayer = game.findPlayer(uuid);

                    applyCombatTimer(uuid, game);

                    if (gamePlayer.isInvisible())
                        revealPlayer(gamePlayer, player);
                    else if (isChargingInvisibility(uuid))
                        cancelInvisibilityCharge(player);
                })
                .build()
        );
        this.eventNode.registerListener(PaperEventListener.builder(EntityDamageByEntityEvent.class, EventPriority.MONITOR)
                .filter(event -> !event.isCancelled())
                .filter(event -> event.getDamageSource().getDamageType() == DamageType.MOB_PROJECTILE)
                .filter(event -> event.getDamager() instanceof Player player && isPlayerClass(game, player, this))
                .handler(event -> {
                    Player player = (Player) event.getDamager();
                    UUID uuid = player.getUniqueId();
                    BouncyBulletGamePlayer gamePlayer = game.findPlayer(uuid);

                    applyCombatTimer(uuid, game);

                    if (gamePlayer.isInvisible())
                        revealPlayer(gamePlayer, player);
                    else if (isChargingInvisibility(uuid))
                        cancelInvisibilityCharge(player);
                })
                .build()
        );
        this.eventNode.registerListener(PaperEventListener.builder(PlayerDeathEvent.class, EventPriority.MONITOR)
                .filter(event -> isPlayerClass(game, event.getPlayer(), this))
                .handler(event -> tryRemoveCombatTimer(event.getPlayer().getUniqueId(), true))
                .build()
        );
    }

    private void applyCombatTimer(UUID uuid, BouncyBulletGame game) {
        BouncyBulletGamePlayer gamePlayer = game.findPlayer(uuid);

        tryRemoveCombatTimer(uuid, false);

        this.combatTimerTasks.put(
                uuid,
                new CombatTimerTask(uuid, gamePlayer.getHud(), COMBAT_TIMER_SECONDS)
                        .start(game.getPlugin())
        );
    }

    private void tryRemoveCombatTimer(UUID uuid, boolean cleanup) {
        CombatTimerTask task = this.combatTimerTasks.remove(uuid);

        if (task != null) {
            task.cancel();

            if (cleanup)
                task.cleanupHud();
        }
    }

    private void revealPlayer(BouncyBulletGamePlayer gamePlayer, Entity player) {
        gamePlayer.reveal();
        player.getWorld().playSound(REVEAL_SOUND, player);
    }

    private void hidePlayer(BouncyBulletGamePlayer gamePlayer, Entity player) {
        gamePlayer.hide();
        player.getWorld().playSound(HIDE_SOUND, player);

    }

    private void startInvisibilityCharge(BouncyBulletGamePlayer gamePlayer, Player player, Plugin plugin) {
        UUID uuid = player.getUniqueId();
        InvisibilityChargeTask existingTask = this.invisChargeTasks.remove(uuid);

        if (existingTask != null)
            existingTask.cancel();

        this.invisChargeTasks.put(
                uuid,
                new InvisibilityChargeTask(gamePlayer, player, INVIS_CHARGE_TICKS)
                        .start(plugin)
        );
    }

    private void cancelInvisibilityCharge(Player player) {
        InvisibilityChargeTask invisChargeTask = this.invisChargeTasks.remove(player.getUniqueId());

        if (invisChargeTask == null)
            return;

        invisChargeTask.cancel();

        player.setExp(0);
        player.setLevel(0);
    }

    private boolean isChargingInvisibility(UUID uuid) {
        return this.invisChargeTasks.containsKey(uuid);
    }

    public static boolean canGoInvisible(BouncyBulletGamePlayer gamePlayer) {
        Player player = gamePlayer.getPlayer();

        if (gamePlayer.isInvisible()) {
            player.sendMessage(Component.text("You are already invisible!", NamedTextColor.RED));
            player.playSound(CHARGE_DENIED_SOUND, Sound.Emitter.self());
            return false;
        }

        if (player.isSprinting()) {
            player.sendMessage(Component.text("You can't sprint while being invisible!", NamedTextColor.RED));
            player.playSound(CHARGE_DENIED_SOUND, Sound.Emitter.self());
            return false;
        }

        return true;
    }

    private class InvisibilityChargeTask extends BukkitRunnable {

        private int tick;
        private final int chargeTicks;
        private final Player player;
        private final BouncyBulletGamePlayer gamePlayer;

        InvisibilityChargeTask(BouncyBulletGamePlayer gamePlayer, Player player, int chargeTicks) {
            this.gamePlayer = gamePlayer;
            this.player = player;
            this.chargeTicks = chargeTicks;
            this.tick = 0;
        }

        @Override
        public void run() {
            float progress = (float) this.tick / this.chargeTicks;

            if (progress < 1) {
                this.player.setExp(progress);

                if (this.tick % 10 == 0) {
                    this.player.playSound(
                            Sound.sound(
                                    Key.key("block.note_block.hat"),
                                    Sound.Source.PLAYER,
                                    1, 1 + (0.5F * progress)
                            ),
                            Sound.Emitter.self()
                    );
                }

            } else {
                this.player.setExp(0);

                StealthPlayerClass.this.hidePlayer(this.gamePlayer, this.player);
                StealthPlayerClass.this.invisChargeTasks.remove(player.getUniqueId(), this);

                this.cancel();
            }

            this.tick++;
        }

        private InvisibilityChargeTask start(Plugin plugin) {
            super.runTaskTimer(plugin, 0, 1);
            return this;
        }
    }

    private class CombatTimerTask extends BukkitRunnable {

        private static final String COMBAT_TIMER_DISPLAY_ID = "combat_timer";

        private int secondsLeft;
        private final BouncyBulletsHUD hud;
        private final UUID uuid;

        CombatTimerTask(UUID uuid, BouncyBulletsHUD hud, int combatTimeSeconds) {
            this.uuid = uuid;
            this.hud = hud;
            this.secondsLeft = combatTimeSeconds;

            if (!hud.hasDisplay(COMBAT_TIMER_DISPLAY_ID))
                hud.addDisplay(COMBAT_TIMER_DISPLAY_ID, combatTimerText(combatTimeSeconds));
        }

        private static Component combatTimerText(int seconds) {
            return Component.text("In Combat: ", NamedTextColor.RED)
                    .append(Component.text(seconds + "s", NamedTextColor.WHITE));
        }

        @Override
        public void run() {
            if (this.secondsLeft <= 0) {
                cleanupHud();
                StealthPlayerClass.this.combatTimerTasks.remove(this.uuid, this);

                this.cancel();
                return;
            }

            this.hud.updateDisplay(COMBAT_TIMER_DISPLAY_ID, combatTimerText(this.secondsLeft));
            this.hud.updateHudText();

            this.secondsLeft--;
        }

        private CombatTimerTask start(Plugin plugin) {
            this.runTaskTimer(plugin, 0, 20);
            return this;
        }

        private void cleanupHud() {
            this.hud.removeDisplay(COMBAT_TIMER_DISPLAY_ID);
            this.hud.updateHudText();
        }
    }
}
