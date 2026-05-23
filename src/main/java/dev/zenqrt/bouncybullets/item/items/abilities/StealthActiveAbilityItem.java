package dev.zenqrt.bouncybullets.item.items.abilities;

import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import dev.zenqrt.bouncybullets.loadout.kit.StealthPlayerClass;
import dev.zenqrt.bouncybullets.utils.MiniMessageUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.List;

public final class StealthActiveAbilityItem extends ActiveAbilityItem {

    private static final int COOLDOWN_TICKS = 900;                  // 45 seconds
    private static final int INVISIBILITY_DURATION_TICKS = 100;     // 5 seconds
    private static final Sound REFILL_SOUND = Sound.sound(org.bukkit.Sound.BLOCK_BREWING_STAND_BREW, Sound.Source.PLAYER, 1, 1);
    private static final Sound DRINK_SOUND = Sound.sound(Key.key("entity.generic.drink"), Sound.Source.PLAYER, 1, 1);

    public StealthActiveAbilityItem() {
        super("stealth_active_ability",
                Material.POTION,
                "Invisibility Cloak",
                MiniMessageUtils.wordWrapLore(
                        List.of(
                                "<gray>Upon right click, become invisible for <green>" + (INVISIBILITY_DURATION_TICKS / 20) + "s</green> seconds.",

                                "",
                                "<dark_gray>Cooldown: <green>" + (COOLDOWN_TICKS / 20) + "s"
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
        BouncyBulletGamePlayer gamePlayer = game.findPlayer(player.getUniqueId());

        if (!StealthPlayerClass.canGoInvisible(gamePlayer))
            return;

        gamePlayer.hide();
        player.playSound(DRINK_SOUND);

        Bukkit.getScheduler().runTaskLater(
                game.getPlugin(),
                gamePlayer::reveal,
                INVISIBILITY_DURATION_TICKS
        );

        ItemStack usedItem = new ItemStack(Material.GLASS_BOTTLE);
        usedItem.setItemMeta(itemStack.getItemMeta());

        player.getInventory().setItemInMainHand(usedItem);
        player.setCooldown(Material.GLASS_BOTTLE, COOLDOWN_TICKS);

        int slotInteracted = event.getPlayer().getInventory().getHeldItemSlot();

        Bukkit.getScheduler().runTaskLater(
                game.getPlugin(),
                () -> {
                    player.getInventory().setItem(slotInteracted, itemStack.clone());
                    player.playSound(REFILL_SOUND, Sound.Emitter.self());
                },
                COOLDOWN_TICKS
        );
    }
}
