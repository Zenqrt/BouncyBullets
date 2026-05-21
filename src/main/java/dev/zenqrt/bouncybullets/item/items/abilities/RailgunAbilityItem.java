package dev.zenqrt.bouncybullets.item.items.abilities;

import dev.zenqrt.bouncybullets.BouncyBulletsPlugin;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.utils.AdventureUtils;
import dev.zenqrt.bouncybullets.utils.ExplosionUtils;
import dev.zenqrt.bouncybullets.utils.MiniMessageUtils;
import dev.zenqrt.bouncybullets.utils.SoundUtils;
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

public final class RailgunAbilityItem extends ActiveAbilityItem {


    private static final int COOLDOWN = 2400;
    private static final double DAMAGE = 15;
    private static final int CHARGING_TICKS = 60;
    private static final Sound CHARGING_SOUND = Sound.sound(org.bukkit.Sound.BLOCK_BEACON_ACTIVATE, Sound.Source.PLAYER, 1, 1);
    private final Map<UUID, ItemStack[]> inventoryContents = new HashMap<>();
    private final List<UUID> isCharging = new ArrayList<>();

    public RailgunAbilityItem() {
        super("demoman_active_ability",
                Material.ECHO_SHARD,
                AdventureUtils.withoutItalics("Pocket Railgun", NamedTextColor.LIGHT_PURPLE)
                        .append(AdventureUtils.withoutItalics(" (Right Click)", NamedTextColor.GRAY)),
                MiniMessageUtils.wordWrapLore(List.of(
                        "<gray>Fire a powerful projectile that creates a large explosion on impact, dealing <red>" + DAMAGE + "❤ <gray>damage to enemies in the area.",
                        "",
                        "<dark_gray>Cooldown: <green>" + (COOLDOWN / 20) + "s"
                ), 30)
        );
    }

    @Override
    public void onUse(BouncyBulletGame game, Player player, ItemStack itemStack, PlayerInteractEvent event) {
        if (this.isCharging.contains(player.getUniqueId())) {
            return;
        }

        this.isCharging.add(player.getUniqueId());

        PlayerInventory inventory = player.getInventory();

        this.inventoryContents.put(player.getUniqueId(), inventory.getContents());

        inventory.clear();
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
        }

        ItemStack chargingItem = new ItemStack(this.material);
        chargingItem.editMeta(meta -> {
            meta.displayName(AdventureUtils.withoutItalics("Charging...", NamedTextColor.GRAY));
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        });

        inventory.setItem(4, chargingItem);
        inventory.setHeldItemSlot(4);

        SoundUtils.playSoundFromPlayer(player, CHARGING_SOUND);

        new ChargingTask(player, 4, CHARGING_TICKS).runTaskTimer(BouncyBulletsPlugin.getInstance(), 0, 1);
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
            if (shooter.isDead() || shooter.getInventory().getHeldItemSlot() != itemSlot) {
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

            if (Objects.requireNonNull(shooter.getInventory().getItem(chargeSlot)).getType() != Material.LIME_STAINED_GLASS_PANE) {
                shooter.playSound(Sound.sound(org.bukkit.Sound.BLOCK_NOTE_BLOCK_HAT.key(), Sound.Source.PLAYER, 1, 1 + (0.5F * tickProgress)), Sound.Emitter.self());
            }

            ItemStack chargeItem = new ItemStack(Material.LIME_STAINED_GLASS_PANE);

            shooter.getInventory().setItem(chargeSlot, chargeItem);
            shooter.getInventory().setItem(8 - chargeSlot, chargeItem);

            currentTick++;
        }

        private void endAbility() {
            shooter.getInventory().setContents(inventoryContents.get(shooter.getUniqueId()));
            isCharging.remove(shooter.getUniqueId());
            inventoryContents.remove(shooter.getUniqueId());

            shooter.setCooldown(material, COOLDOWN);
        }

        private void shoot() {
            Location location = shooter.getEyeLocation();

            SoundUtils.playSoundFromPlayer(shooter, SHOOT_SOUND);

            RayTraceResult result = location.getWorld().rayTrace(location, location.getDirection(), 100, FluidCollisionMode.NEVER, true, BEAM_RADIUS, entity -> entity instanceof Player player && player.getGameMode() == GameMode.ADVENTURE && player != shooter);

            if (result == null) {
                return;
            }

            Location hitLocation = result.getHitPosition().toLocation(location.getWorld());
            ExplosionUtils.createExplosion(hitLocation, 4, DAMAGE, DamageSource.builder(DamageType.MOB_PROJECTILE).withCausingEntity(shooter).build());
        }
    }

}
