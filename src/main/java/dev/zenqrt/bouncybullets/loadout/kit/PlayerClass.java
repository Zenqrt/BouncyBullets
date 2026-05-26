package dev.zenqrt.bouncybullets.loadout.kit;

import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import dev.zenqrt.bouncybullets.item.items.abilities.ActiveAbilityItem;
import dev.zenqrt.bouncybullets.item.items.guns.GunItem;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public interface PlayerClass {
    String getName();
    List<GunItem> getGuns();
    List<ActiveAbilityItem> getActiveAbilities();
    Map<EquipmentSlot, ItemStack> getArmorEquipment();

    default void onStartUse(BouncyBulletGamePlayer gamePlayer) {}
    default void onStopUse(BouncyBulletGamePlayer gamePlayer) {}
    default void onRespawn(BouncyBulletGamePlayer gamePlayer) {}
}
