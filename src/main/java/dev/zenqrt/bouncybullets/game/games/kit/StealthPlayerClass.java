package dev.zenqrt.bouncybullets.game.games.kit;

import dev.zenqrt.bouncybullets.BouncyBullets;
import dev.zenqrt.bouncybullets.game.event.PaperEventListener;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletPlayer;
import dev.zenqrt.bouncybullets.game.games.Gun;
import dev.zenqrt.bouncybullets.item.GameItem;
import dev.zenqrt.bouncybullets.item.items.ActiveAbilityItem;
import dev.zenqrt.bouncybullets.utils.AdventureUtils;
import dev.zenqrt.bouncybullets.utils.MiniMessageUtils;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

final class StealthPlayerClass extends EventPlayerClass {

    private static final Gun PRIMARY_GUN = Gun.SMG;
    private static final Gun SECONDARY_GUN = Gun.SILENCED_PISTOL;
    private static final InvisibilityAbility ACTIVE_ABILITY = new InvisibilityAbility();

    @Override
    public String getName() {
        return "Stealth";
    }

    @Override
    public TreeMap<Integer, ItemStack> getItems() {
        TreeMap<Integer, ItemStack> items = new TreeMap<>();

        items.put(0, PRIMARY_GUN.buildItemStack());
        items.put(1, SECONDARY_GUN.buildItemStack());
        items.put(2, ACTIVE_ABILITY.buildItemStack());

        return items;
    }

    @Override
    public void registerEvents(BouncyBulletPlayer player) {
        GameItem.registerGameItemEvents(List.of(ACTIVE_ABILITY));
        this.eventNode.registerListener(PaperEventListener.builder(PlayerDeathEvent.class)
                .filter(event -> {
                    EntityDamageEvent lastDamageEvent = event.getPlayer().getLastDamageCause();

                    if (lastDamageEvent != null) {
                        return lastDamageEvent.getDamageSource().getCausingEntity().equals(player.player());
                    }

                    return false;
                })
                .handler(event -> player.player().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 5, 2, false, true, true))).build());
    }

    private static class InvisibilityAbility extends ActiveAbilityItem {

        private static final int COOLDOWN = 1200;
        private static final Sound REFILL_SOUND = Sound.sound(org.bukkit.Sound.BLOCK_BREWING_STAND_BREW, Sound.Source.PLAYER, 1, 1);

        InvisibilityAbility() {
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
        public void onUse(PlayerInteractEvent event) {
            Player player = event.getPlayer();

            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 1, false, false, true));
            player.playSound(Sound.sound(org.bukkit.Sound.ENTITY_GENERIC_DRINK, Sound.Source.PLAYER, 1, 1));

            ItemStack originalItem = Objects.requireNonNull(event.getItem());

            ItemStack usedItem = new ItemStack(Material.GLASS_BOTTLE);
            usedItem.setItemMeta(originalItem.getItemMeta());

            player.getInventory().setItemInMainHand(usedItem);
            player.setCooldown(Material.GLASS_BOTTLE, COOLDOWN);

            int slotInteracted = event.getPlayer().getInventory().getHeldItemSlot();

            new BukkitRunnable() {
                @Override
                public void run() {
                    player.getInventory().setItem(slotInteracted, originalItem.clone());
                    player.playSound(REFILL_SOUND, Sound.Emitter.self());
                }
            }.runTaskLater(BouncyBullets.getInstance(), COOLDOWN);
        }
    }
}
