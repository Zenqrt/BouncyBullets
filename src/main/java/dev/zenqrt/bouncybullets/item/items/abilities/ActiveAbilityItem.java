package dev.zenqrt.bouncybullets.item.items.abilities;

import dev.zenqrt.bouncybullets.game.games.BouncyBulletGame;
import dev.zenqrt.bouncybullets.item.GameItem;
import dev.zenqrt.bouncybullets.utils.AdventureUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.function.Consumer;

public abstract class ActiveAbilityItem extends GameItem {

    public ActiveAbilityItem(String key, Material material, String displayName, List<Component> description, Consumer<ItemMeta> itemMetaHandler) {
        super(
                key,
                material,
                AdventureUtils.withoutItalics(displayName, NamedTextColor.LIGHT_PURPLE)
                        .append(Component.text(" (Right Click)", NamedTextColor.GRAY)),
                description,
                itemMetaHandler
        );
    }

    public ActiveAbilityItem(String key, Material material, String displayName, List<Component> description) {
        super(
                key,
                material,
                AdventureUtils.withoutItalics(displayName, NamedTextColor.LIGHT_PURPLE)
                        .append(Component.text(" (Right Click)", NamedTextColor.GRAY)),
                description);
    }

    public abstract void onUse(BouncyBulletGame game, Player player, ItemStack itemStack, PlayerInteractEvent event);

    @Override
    public void onInteract(BouncyBulletGame game, Player player, ItemStack itemStack, PlayerInteractEvent event) {
        event.setCancelled(true);

        if (player.hasCooldown(itemStack.getType()))
            return;

        this.onUse(game, player, itemStack, event);
    }
}
