package dev.zenqrt.bouncybullets.loadout.kit;

import dev.zenqrt.bouncybullets.game.games.BouncyBulletGamePlayer;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.TreeMap;

public interface PlayerClass {
    String getName();
    TreeMap<Integer, ItemStack> getItems();
    Map<EquipmentSlot, ItemStack> getArmorEquipment();

    default void onStartUse(BouncyBulletGamePlayer gamePlayer) {}
    default void onStopUse(BouncyBulletGamePlayer gamePlayer) {}
    default void onRespawn(BouncyBulletGamePlayer gamePlayer) {}
}
