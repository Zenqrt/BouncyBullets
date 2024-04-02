package dev.zenqrt.bouncybullets.events;

import org.bukkit.damage.DamageType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public final class PlayerListeners implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerFoodLevelChange(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (event.getDamageSource().getDamageType() != DamageType.MOB_PROJECTILE) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerHandSwapItems(PlayerSwapHandItemsEvent event) {
        event.setCancelled(true);
    }

}
