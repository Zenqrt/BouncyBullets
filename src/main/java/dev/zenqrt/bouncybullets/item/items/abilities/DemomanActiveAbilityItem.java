package dev.zenqrt.bouncybullets.item.items.abilities;

import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.utils.AdventureUtils;
import dev.zenqrt.bouncybullets.utils.ExplosionUtils;
import dev.zenqrt.bouncybullets.utils.MiniMessageUtils;
import dev.zenqrt.bouncybullets.utils.SoundUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;

import java.util.*;

public final class DemomanActiveAbilityItem extends ActiveAbilityItem {

    private static final int COOLDOWN = 1200;
    private static final double DAMAGE = 15;
    private static final int CHARGING_TICKS = 40;
    private static final double EXPLOSION_RADIUS = 4;
    private static final Sound CHARGING_SOUND = Sound.sound(org.bukkit.Sound.BLOCK_BEACON_ACTIVATE, Sound.Source.PLAYER, 1, 1);

    private final Map<UUID, ItemStack[]> inventoryContents = new HashMap<>();
    private final List<UUID> isCharging = new ArrayList<>();

    public DemomanActiveAbilityItem() {
        super("demoman_active_ability",
                Material.ECHO_SHARD,
                "Pocket Railgun",
                MiniMessageUtils.wordWrapLore(List.of(
                        "<gray>Fire a powerful projectile that creates a large explosion on impact, dealing <red>" + DAMAGE + "❤ <gray>damage to enemies within a radius of <green>" + EXPLOSION_RADIUS + " blocks<gray>.",
                        "",
                        "<dark_gray>Cooldown: <green>" + (COOLDOWN / 20) + "s"
                ), 30)
        );
    }

    @Override
    public void onUse(BouncyBulletGame game, Player player, ItemStack itemStack, PlayerInteractEvent event) {
        UUID uuid = player.getUniqueId();

        if (this.isCharging.contains(uuid))
            return;

        this.isCharging.add(uuid);

        PlayerInventory inventory = player.getInventory();

        this.inventoryContents.put(uuid, inventory.getStorageContents());

        clearStorage(inventory);
        loadAbilityInventory(inventory);

        SoundUtils.playSoundFromPlayer(player, CHARGING_SOUND);

        new ChargingTask(player, 4, CHARGING_TICKS).runTaskTimer(game.getPlugin(), 0, 1);
    }

    private static void clearStorage(PlayerInventory inventory) {
        for (int i = 0; i < inventory.getStorageContents().length; i++) {
            inventory.setItem(i, null);
        }
    }

    private void loadAbilityInventory(PlayerInventory inventory) {
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
        }

        ItemStack chargingItem = new ItemStack(super.material);
        chargingItem.editMeta(meta -> {
            meta.displayName(AdventureUtils.withoutItalics("Charging...", NamedTextColor.GRAY));
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        });

        inventory.setItem(4, chargingItem);
        inventory.setHeldItemSlot(4);
    }

    private class ChargingTask extends BukkitRunnable {

        private static final Sound SHOOT_SOUND = Sound.sound(org.bukkit.Sound.ENTITY_GENERIC_EXPLODE.key(), Sound.Source.PLAYER, 1, 1.5F);
        private static final double BEAM_RADIUS = 1;

        private final Player shooter;
        private final int itemSlot;
        private final int chargingTicks;
        private int currentTick;

        ChargingTask(Player shooter, int itemSlot, int chargingTicks) {
            this.shooter = shooter;
            this.itemSlot = itemSlot;
            this.chargingTicks = chargingTicks;
        }

        @Override
        public void run() {
            if (shooter.getGameMode() == GameMode.SPECTATOR || shooter.getInventory().getHeldItemSlot() != itemSlot) {
                endAbility();

                this.cancel();
                return;
            }

            if (currentTick >= chargingTicks) {
                shoot();
                endAbility();
                this.cancel();
                return;
            }

            float tickProgress = (float) currentTick / chargingTicks;
            int chargeSlot = (int) (4 * tickProgress);

            if (Objects.requireNonNull(shooter.getInventory().getItem(chargeSlot)).getType() != Material.LIME_STAINED_GLASS_PANE)
                shooter.playSound(
                        Sound.sound(
                                Key.key("block.note_block.hat"),
                                Sound.Source.PLAYER,
                                1, 1 + (0.5F * tickProgress)
                        ),
                        Sound.Emitter.self()
                );

            ItemStack chargeItem = new ItemStack(Material.LIME_STAINED_GLASS_PANE);

            shooter.getInventory().setItem(chargeSlot, chargeItem);
            shooter.getInventory().setItem(8 - chargeSlot, chargeItem);

            currentTick++;
        }

        private void endAbility() {
            shooter.getInventory().setStorageContents(inventoryContents.get(shooter.getUniqueId()));
            isCharging.remove(shooter.getUniqueId());
            inventoryContents.remove(shooter.getUniqueId());

            shooter.setCooldown(material, COOLDOWN);
        }

        @SuppressWarnings("UnstableApiUsage")
        private void shoot() {
            Location location = shooter.getEyeLocation();

            SoundUtils.playSoundFromPlayer(shooter, SHOOT_SOUND);

            RayTraceResult result = location.getWorld().rayTrace(
                    location,
                    location.getDirection(),
                    100,
                    FluidCollisionMode.NEVER,
                    true,
                    BEAM_RADIUS,
                    entity -> entity instanceof Player player && player.getGameMode() == GameMode.ADVENTURE && player != shooter
            );

            if (result == null)
                return;

            Location hitLocation = result.getHitPosition().toLocation(location.getWorld());
            ExplosionUtils.createExplosion(
                    hitLocation,
                    EXPLOSION_RADIUS,
                    DAMAGE,
                    DamageSource.builder(DamageType.MOB_PROJECTILE)
                            .withCausingEntity(shooter)
                            .build()
            );
        }
    }

}
