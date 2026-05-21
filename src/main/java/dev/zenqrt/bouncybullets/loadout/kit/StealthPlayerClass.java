package dev.zenqrt.bouncybullets.loadout.kit;

import dev.zenqrt.bouncybullets.event.PaperEventListener;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import dev.zenqrt.bouncybullets.item.GameItems;
import dev.zenqrt.bouncybullets.item.items.abilities.ActiveAbilityItem;
import dev.zenqrt.bouncybullets.item.items.guns.GunItem;
import dev.zenqrt.bouncybullets.utils.ItemUtils;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.TreeMap;

final class StealthPlayerClass extends EventPlayerClass {

    private static final GunItem PRIMARY_GUN = GameItems.SMG;
    private static final GunItem SECONDARY_GUN = GameItems.SILENCED_PISTOL;
    private static final ActiveAbilityItem ACTIVE_ABILITY = GameItems.STEALTH_ACTIVE_ABILITY;

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
    public Map<EquipmentSlot, ItemStack> getArmorEquipment() {
        return Map.of(
                EquipmentSlot.HEAD, ItemUtils.createLeatherArmor(Material.LEATHER_HELMET, Color.BLACK),
                EquipmentSlot.LEGS, new ItemStack(Material.CHAINMAIL_LEGGINGS),
                EquipmentSlot.FEET, ItemUtils.createLeatherArmor(Material.LEATHER_BOOTS, Color.fromRGB(43, 43, 43))
        );
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void registerEvents(BouncyBulletGame game) {
        this.eventNode.registerListener(PaperEventListener.builder(PlayerDeathEvent.class)
                .filter(event -> {
                    Player player = event.getPlayer();
                    EntityDamageEvent lastDamageEvent = player.getLastDamageCause();

                    if (lastDamageEvent != null && lastDamageEvent.getDamageSource().getCausingEntity() instanceof Player causePlayer) {
                        if (!game.hasPlayer(causePlayer.getUniqueId()))
                            return false;

                        BouncyBulletGamePlayer gamePlayer = game.findPlayer(causePlayer.getUniqueId());

                        return gamePlayer.getLoadout().playerClass() == this;
                    }

                    return false;
                })
                .handler(event -> event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 5, 2, false, true, true))).build());
    }
}
