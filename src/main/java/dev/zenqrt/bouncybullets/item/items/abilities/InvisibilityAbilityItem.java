package dev.zenqrt.bouncybullets.item.items.abilities;

import dev.zenqrt.bouncybullets.BouncyBulletsPlugin;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.utils.AdventureUtils;
import dev.zenqrt.bouncybullets.utils.MiniMessageUtils;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public final class InvisibilityAbilityItem extends ActiveAbilityItem {

    private static final int COOLDOWN = 1200;
    private static final Sound REFILL_SOUND = Sound.sound(org.bukkit.Sound.BLOCK_BREWING_STAND_BREW, Sound.Source.PLAYER, 1, 1);

    public InvisibilityAbilityItem() {
        super("stealth_active_ability",
                Material.POTION,
                AdventureUtils.withoutItalics("Invisibility Cloak", NamedTextColor.LIGHT_PURPLE)
                        .append(Component.text(" (Right Click)", NamedTextColor.GRAY)),
                MiniMessageUtils.wordWrapLore(
                        List.of(
                                "<gray>Upon right click, become invisible for <green>5</green> seconds.",
                                "",
                                "<dark_gray>Cooldown: <green>" + (COOLDOWN / 20) + "s"
                        ),
                        30
                ), meta -> {
                    PotionMeta potionMeta = (PotionMeta) meta;
                    potionMeta.setBasePotionType(PotionType.INVISIBILITY);
                    potionMeta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
                });
    }

    @Override
    public void onUse(BouncyBulletGame game, Player player, ItemStack itemStack, PlayerInteractEvent event) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 1, false, false, true));
        player.playSound(Sound.sound(org.bukkit.Sound.ENTITY_GENERIC_DRINK, Sound.Source.PLAYER, 1, 1));

        ItemStack usedItem = new ItemStack(Material.GLASS_BOTTLE);
        usedItem.setItemMeta(itemStack.getItemMeta());

        player.getInventory().setItemInMainHand(usedItem);
        player.setCooldown(Material.GLASS_BOTTLE, COOLDOWN);

        int slotInteracted = event.getPlayer().getInventory().getHeldItemSlot();

        new BukkitRunnable() {
            @Override
            public void run() {
                player.getInventory().setItem(slotInteracted, itemStack.clone());
                player.playSound(REFILL_SOUND, Sound.Emitter.self());
            }
        }.runTaskLater(BouncyBulletsPlugin.getInstance(), COOLDOWN);
    }
}
