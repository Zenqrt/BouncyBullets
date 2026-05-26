package dev.zenqrt.bouncybullets.item.items.abilities;

import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import dev.zenqrt.bouncybullets.utils.MiniMessageUtils;
import dev.zenqrt.bouncybullets.utils.Sounds;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public final class HeavyActiveAbilityItem extends ActiveAbilityItem implements Listener {

    private static final int COOLDOWN_TICKS = 1800;                 // 90 seconds
    private static final int ABILITY_DURATION_TICKS = 300;          // 15 seconds
    private static final float DEFLECT_CHANCE = 0.9F;
    private static final Sound ACTIVATE_SOUND = Sound.sound(Sounds.ENTITY_ZOMBIE_VILLAGER_CURE, Sound.Source.PLAYER, 1, 0.75F);
    private static final Sound DEACTIVATE_SOUND = Sound.sound(Sounds.BLOCK_BEACON_AMBIENT, Sound.Source.PLAYER, 1, 0.8F);
    private static final Sound DEFLECT_SOUND = Sound.sound(Sounds.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, Sound.Source.PLAYER, 1, 2);

    private final Set<UUID> abilityActive = new HashSet<>();

    public HeavyActiveAbilityItem() {
        super(
                "heavy_active_ability",
                Material.HEART_OF_THE_SEA,
                "I AM BULLETPROOF!! (almost)",
                MiniMessageUtils.wordWrapLore(
                        List.of(
                                "<gray>Gain a <red>90% <gray>chance to deflect any bullet that hits you for <green>" + (ABILITY_DURATION_TICKS / 20) + "s<gray>.",
                                "",
                                "<dark_gray>Cooldown: <green>" + (COOLDOWN_TICKS / 20) + "s"
                        ),
                        30
                )
        );
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || !this.abilityActive.contains(player.getUniqueId()))
            return;

        if (cannotDeflect(ThreadLocalRandom.current()))
            return;

        event.setCancelled(true);
        player.getWorld().playSound(DEFLECT_SOUND, player);
    }

    private static boolean cannotDeflect(Random random) {
        return random.nextFloat() > DEFLECT_CHANCE;
    }

    @Override
    public void onUse(BouncyBulletGame game, Player player, ItemStack itemStack, PlayerInteractEvent event) {
        UUID uuid = player.getUniqueId();

        this.abilityActive.add(uuid);

        addGlowToArmor(player.getInventory());
        player.getWorld().playSound(ACTIVATE_SOUND, player);

        BouncyBulletGamePlayer gamePlayer = game.findPlayerOrThrow(uuid);

        Bukkit.getScheduler().runTaskLater(
                game.getPlugin(),
                () -> {
                    if (gamePlayer.isDead())
                        return;

                    this.abilityActive.remove(uuid);

                    removeGlowFromArmor(player.getInventory());
                    player.getWorld().playSound(DEACTIVATE_SOUND, player);
                },
                ABILITY_DURATION_TICKS
        );

        player.setCooldown(super.material, COOLDOWN_TICKS);
    }

    private static void addGlowToArmor(PlayerInventory inventory) {
        for (ItemStack armorItem : inventory.getArmorContents()) {
            if (armorItem == null)
                continue;

            armorItem.editMeta(meta -> meta.addEnchant(Enchantment.UNBREAKING, 1, false));
        }
    }

    private static void removeGlowFromArmor(PlayerInventory inventory) {
        for (ItemStack armorItem : inventory.getArmorContents()) {
            if (armorItem == null)
                continue;

            armorItem.editMeta(meta -> meta.removeEnchant(Enchantment.UNBREAKING));
        }
    }

}
