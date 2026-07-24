package dev.zenqrt.bouncybullets.event.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.object.ObjectContents;
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

//    @EventHandler(priority = EventPriority.HIGHEST)
//    public void onEntityDamageEvent(EntityDamageEvent event) {
//        if (event.getDamageSource().getDamageType() != DamageType.MOB_PROJECTILE) {
//            event.setCancelled(true);
//        }
//    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerHandSwapItems(PlayerSwapHandItemsEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        event.renderer(((source, displayName, message, _) ->
                Component.object(ObjectContents.playerHead(source.getPlayerProfile()))
                        .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                        .append(displayName)
                        .append(Component.text(": ", NamedTextColor.WHITE))
                        .append(message)
                )
        );
    }

}
